#ifdef GL_ES
    precision mediump float;
#endif

varying vec4 v_color;
varying vec2 v_texCoords;
uniform sampler2D u_texture;
uniform float u_input0;
uniform vec2 u_resolution;

void main() {
    vec4 c = v_color * texture2D(u_texture, v_texCoords);
    float gamma = u_input0;
    c.rgb = pow(c.rgb, vec3(1.0 / gamma));
    gl_FragColor = c;
}