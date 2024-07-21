#ifdef GL_ES
    precision mediump float;
#endif

varying vec4 v_color;
varying vec2 v_texCoords;
uniform sampler2D u_texture;
uniform vec2 u_resolution;

const float radius = 1.;
const float increment = .1;

void main() {
    vec2 relativePosition = gl_FragCoord.xy / u_resolution - .5;
    vec2 onePixel = vec2(1.0 / u_resolution.x, 1.0 / u_resolution.y);
    vec2 uv = v_texCoords;

    vec4 c = v_color * texture2D(u_texture, v_texCoords);

    if (c.a == 0.0) {
        gl_FragColor = c;
        return;
    }

    bool isEdge = false;
    for (float i = -radius; i <= radius; i+=increment) {
        for (float j = -radius; j <= radius; j+=increment) {
            vec2 offset = vec2(float(i), float(j)) * onePixel;
            vec4 sample = texture2D(u_texture, uv + offset);
            if (sample.a == .0) {
                isEdge = true;
                break;
            }
        }
    }

    if (isEdge) {
        gl_FragColor = vec4(1.0, 1.0, 1.0, 1.0);
        return;
    } else {
        gl_FragColor = vec4(0.0, 0.0, 0.0, 1.0);
        return;
    }
}