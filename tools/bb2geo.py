"""
Converts a Blockbench GeckoLib project (.bbmodel) into a GeckoLib model (.geo.json).

Because idk why but he Blockbench exporter is broken

Usage: python tools/bb2geo.py <input.bbmodel> <output.geo.json>
"""
import json
import sys

FACES = ("north", "east", "south", "west", "up", "down")


def is_set(vector):
    return any(abs(v) > 1e-9 for v in vector)


def clean(value):
    if isinstance(value, float):
        value = round(value, 4)
        return int(value) if value.is_integer() else value
    if isinstance(value, list):
        return [clean(v) for v in value]
    if isinstance(value, dict):
        return {k: clean(v) for k, v in value.items()}
    return value


def convert_uv(element):
    if element.get("box_uv"):
        return element.get("uv_offset", [0, 0])

    uv = {}
    for name in FACES:
        face = element.get("faces", {}).get(name)
        if not face or face.get("texture") is None:
            continue
        u = face["uv"]
        # Blockbench stores up/down flipped compared to the geo format
        if name in ("up", "down"):
            uv[name] = {"uv": [u[2], u[3]], "uv_size": [u[0] - u[2], u[1] - u[3]]}
        else:
            uv[name] = {"uv": [u[0], u[1]], "uv_size": [u[2] - u[0], u[3] - u[1]]}
        if face.get("rotation"):
            uv[name]["uv_rotation"] = face["rotation"]
    return uv


def convert_cube(element):
    start, end = element["from"], element["to"]
    cube = {
        "origin": [-end[0], start[1], start[2]],
        "size": [end[i] - start[i] for i in range(3)],
        "uv": convert_uv(element),
    }
    if element.get("inflate"):
        cube["inflate"] = element["inflate"]
    rotation = element.get("rotation", [0, 0, 0])
    if is_set(rotation):
        pivot = element.get("origin", [0, 0, 0])
        cube["pivot"] = [-pivot[0], pivot[1], pivot[2]]
        cube["rotation"] = [-rotation[0], -rotation[1], rotation[2]]
    if element.get("mirror_uv"):
        cube["mirror"] = True
    return cube


def convert(path):
    with open(path, encoding="utf-8") as file:
        project = json.load(file)

    groups = {group["uuid"]: group for group in project.get("groups", [])}
    elements = {element["uuid"]: element for element in project["elements"]}
    bones = []

    def walk(node, parent):
        group = groups.get(node.get("uuid"), node)
        origin = group.get("origin", [0, 0, 0])
        bone = {"name": group["name"]}
        if parent:
            bone["parent"] = parent
        bone["pivot"] = [-origin[0], origin[1], origin[2]]
        rotation = group.get("rotation", [0, 0, 0])
        if is_set(rotation):
            bone["rotation"] = [-rotation[0], -rotation[1], rotation[2]]

        cubes = [convert_cube(elements[child]) for child in node.get("children", [])
                 if isinstance(child, str) and child in elements]
        if cubes:
            bone["cubes"] = cubes
        bones.append(bone)

        for child in node.get("children", []):
            if isinstance(child, dict):
                walk(child, group["name"])

    for node in project["outliner"]:
        if isinstance(node, dict):
            walk(node, None)

    identifier = project.get("model_identifier") or project.get("name", "model")
    return {
        "format_version": "1.12.0",
        "minecraft:geometry": [{
            "description": {
                "identifier": "geometry." + identifier,
                "texture_width": project["resolution"]["width"],
                "texture_height": project["resolution"]["height"],
                "visible_bounds_width": 4,
                "visible_bounds_height": 2.5,
                "visible_bounds_offset": [0, 0.75, 0],
            },
            "bones": bones,
        }],
    }


def main():
    if len(sys.argv) != 3:
        print("Usage: python tools/bb2geo.py <input.bbmodel> <output.geo.json>")
        sys.exit(1)
    with open(sys.argv[2], "w", encoding="utf-8") as file:
        json.dump(clean(convert(sys.argv[1])), file, indent=1)
    print("Wrote " + sys.argv[2])


if __name__ == "__main__":
    main()
