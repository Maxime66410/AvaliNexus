"""
Converts the animations of a CPM project (.cpmproject) into a GeckoLib animation file.

The Blockbench export loses the CPM frame timing and the smooth loop back to the first frame,
so we read the animations straight from the CPM project instead.
Shitty export again :c

Usage: python tools/cpm2anim.py <input.cpmproject> <output.animation.json>
"""
import json
import re
import sys
import zipfile

VANILLA_PARTS = ["head", "body", "left_arm", "right_arm", "left_leg", "right_leg"]


def read_json(archive, name):
    raw = archive.read(name)
    try:
        return json.loads(raw.decode("utf-8"))
    except UnicodeDecodeError:
        return json.loads(raw.decode("latin-1"))


def collect_part_names(node, names):
    if isinstance(node, dict):
        if "storeID" in node and "name" in node:
            names[node["storeID"]] = node["name"]
        for value in node.values():
            collect_part_names(value, names)
    elif isinstance(node, list):
        for value in node:
            collect_part_names(value, names)


def animation_name(file_name, data):
    # Gestures and custom poses keep the name shown in CPM, states share names like "Tail" so they use the file name
    name = data.get("name") or ""
    if not file_name.startswith("v_") and name and name.isascii() and name.isprintable():
        return name
    base = re.sub(r"_\d+\.json$", "", file_name)
    return re.sub(r"^[vgc]_", "", base).strip("_")


def unwrap(angles):
    out = []
    for angle in angles:
        angle = (angle + 180.0) % 360.0 - 180.0
        if out:
            while angle - out[-1] > 180.0:
                angle -= 360.0
            while angle - out[-1] < -180.0:
                angle += 360.0
        out.append(angle)
    return out


def closest_turn(value, target):
    while value - target > 180.0:
        value -= 360.0
    while value - target < -180.0:
        value += 360.0
    return value


def keyframes(times, vectors):
    return {
        f"{round(t, 4):g}": {"vector": [round(v, 4) for v in vector], "lerp_mode": "catmullrom"}
        for t, vector in zip(times, vectors)
    }


def convert_animation(data, names, looping):
    frames = data["frames"]
    duration = data["duration"] / 1000.0
    step = duration / len(frames)
    times = [i * step for i in range(len(frames))]

    parts = {}
    for index, frame in enumerate(frames):
        for component in frame["components"]:
            parts.setdefault(component["storeID"], {})[index] = component

    bones = {}
    for store_id, by_frame in parts.items():
        name = VANILLA_PARTS[store_id] if 0 <= store_id < len(VANILLA_PARTS) else names.get(store_id)
        if name is None:
            continue
        # A part missing from a frame stays at its rest pose there
        rotations = [[0.0, 0.0, 0.0]] * len(frames)
        positions = [[0.0, 0.0, 0.0]] * len(frames)
        for index, component in by_frame.items():
            # CPM also pushes hidden parts inside the head, visibility is handled in code so we drop that
            if component.get("show") is False:
                continue
            rotations[index] = [component["rotation"][axis] for axis in "xyz"]
            positions[index] = [component["pos"][axis] for axis in "xyz"]

        rotations = [list(axis) for axis in zip(*(unwrap(list(axis)) for axis in zip(*rotations)))]
        channel_times = list(times)
        if looping:
            channel_times.append(duration)
            rotations.append([closest_turn(first, last) for first, last in zip(rotations[0], rotations[-1])])
            positions.append(positions[0])

        bone = {}
        if any(abs(v) > 1e-6 for vector in rotations for v in vector):
            bone["rotation"] = keyframes(channel_times, rotations)
        if any(abs(v) > 1e-6 for vector in positions for v in vector):
            bone["position"] = keyframes(channel_times, positions)
        if bone:
            bones[name] = bone

    return {"loop": looping, "animation_length": round(duration, 4), "bones": bones}


def convert(path):
    with zipfile.ZipFile(path) as archive:
        names = {}
        collect_part_names(read_json(archive, "config.json"), names)

        animations = {}
        for file in sorted(archive.namelist()):
            if not file.startswith("animations/") or not file.endswith(".json"):
                continue
            file_name = file.split("/", 1)[1]
            data = read_json(archive, file)
            if not data.get("frames"):
                continue
            # Poses (v_) loop for as long as the state lasts, the rest follow their own loop flag
            looping = file_name.startswith("v_") or bool(data.get("loop"))
            animations[animation_name(file_name, data)] = convert_animation(data, names, looping)

    return {"format_version": "1.8.0", "animations": animations}


def main():
    if len(sys.argv) != 3:
        print("Usage: python tools/cpm2anim.py <input.cpmproject> <output.animation.json>")
        sys.exit(1)
    result = convert(sys.argv[1])
    with open(sys.argv[2], "w", encoding="utf-8") as file:
        json.dump(result, file, indent=1, ensure_ascii=False)
    print(f"Wrote {len(result['animations'])} animations to {sys.argv[2]}")


if __name__ == "__main__":
    main()
