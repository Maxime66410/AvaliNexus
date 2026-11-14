#version 330
#extension GL_ARB_separate_shader_objects : require

uniform sampler2D InSampler;
uniform sampler2D DepthSampler;
uniform sampler2D ParamsSampler;

layout(std140) uniform SamplerInfo {
    vec2 OutSize;
    vec2 InSize;
};

layout(std140) uniform VisionConfig {
    vec2 BlurDir;
};

layout(location = 0) in vec2 texCoord;

layout(location = 0) out vec4 fragColor;

const float NEAR = 0.05;
const int MAX_STEPS = 24;
const float MAX_HAZE = 1.5;
const float DESATURATION = 0.25;
const vec3 LUMA = vec3(0.299, 0.587, 0.114);

float linearDepth(float depth, float far) {
#ifdef RENDERPEARL_DEPTH_IS_ZERO_TO_ONE
    return far * NEAR / (NEAR + depth * (far - NEAR));
#else
    float ndc = depth * 2.0 - 1.0;
    return 2.0 * far * NEAR / ((NEAR + far) + ndc * (far - NEAR));
#endif
}

float blurAmount(vec2 uv, float distance, float far) {
    float z = linearDepth(texture(DepthSampler, uv).r, far);
    return clamp((z - distance) / distance, 0.0, 1.0);
}

void main() {
    vec4 params = texelFetch(ParamsSampler, ivec2(0, 0), 0);
    float distance = max(round(params.r * 255.0), 1.0);
    float far = max(round(params.g * 255.0) * 256.0 + round(params.b * 255.0), distance + 1.0);
    float strength = round(params.a * 255.0) / 100.0;

    float amount = blurAmount(texCoord, distance, far);
    float maxRadius = clamp(OutSize.y / 180.0, 2.0, 12.0) * strength;
    float radius = min(amount * maxRadius, float(MAX_STEPS));

    vec4 color;
    if (radius < 0.5) {
        color = texture(InSampler, texCoord);
    } else {
        vec2 texel = BlurDir / InSize;
        float sigma = max(radius * 0.5, 0.5);
        float falloff = 1.0 / (2.0 * sigma * sigma);
        vec4 sum = texture(InSampler, texCoord);
        float total = 1.0;
        for (int i = 1; i <= MAX_STEPS; i += 2) {
            float first = float(i);
            if (first > radius) {
                break;
            }
            float second = first + 1.0;
            float firstWeight = exp(-first * first * falloff);
            float secondWeight = second <= radius ? exp(-second * second * falloff) : 0.0;
            float pairWeight = firstWeight + secondWeight;
            float offset = (first * firstWeight + second * secondWeight) / pairWeight;
            vec2 uvPlus = texCoord + texel * offset;
            vec2 uvMinus = texCoord - texel * offset;
            float weightPlus = pairWeight * blurAmount(uvPlus, distance, far);
            float weightMinus = pairWeight * blurAmount(uvMinus, distance, far);
            sum += texture(InSampler, uvPlus) * weightPlus + texture(InSampler, uvMinus) * weightMinus;
            total += weightPlus + weightMinus;
        }
        color = sum / max(total, 0.0001);
    }

    if (BlurDir.y > 0.5) {
        float haze = clamp(amount * strength, 0.0, MAX_HAZE);
        color.rgb = mix(color.rgb, vec3(dot(color.rgb, LUMA)), DESATURATION * haze);
    }
    fragColor = color;
}
