#version 330

#moj_import <minecraft:fog.glsl>
#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:projection.glsl>

uniform sampler2D Sampler0;

in float sphericalVertexDistance;
in float cylindricalVertexDistance;
in vec4 vertexColor;
in vec2 texCoord0;

out vec4 fragColor;

const float VANILLA_TEXT_COLOR = float(0x404040);
const float NEW_TEXT_COLOR = float(0xaaaaaa);

void main() {
    vec4 color = texture(Sampler0, texCoord0) * vertexColor * ColorModulator;
    if (color.a < 0.1) {
        discard;
    }

    if (ProjMat[2].w == 0.0) {
        vec3 chn = floor(clamp(color.rgb, 0.0, 1.0) * 255.0 + vec3(0.5));
        float col = chn.x * (256.0 * 256.0) + chn.y * 256.0 + chn.z;
        if (col == VANILLA_TEXT_COLOR) {
            color.rgb = vec3(
                floor(NEW_TEXT_COLOR / (256.0 * 256.0)),
                floor(mod(NEW_TEXT_COLOR, 256.0 * 256.0) / 256.0),
                mod(NEW_TEXT_COLOR, 256.0)
            ) / 255.0;
        }
    }

    fragColor = apply_fog(color, sphericalVertexDistance, cylindricalVertexDistance, FogEnvironmentalStart, FogEnvironmentalEnd, FogRenderDistanceStart, FogRenderDistanceEnd, FogColor);
}
