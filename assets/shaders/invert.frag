#ifdef GL_ES
    precision mediump float;
#endif

varying vec4 v_color;
varying vec2 v_texCoords;
uniform sampler2D u_texture;

void main() {
  vec4 c = v_color * texture2D(u_texture, v_texCoords);
  c = 1.0 - c;
  c.a = 1.0 - c.a;
  gl_FragColor = c;
}