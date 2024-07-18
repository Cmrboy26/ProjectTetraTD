#ifdef GL_ES
    precision mediump float;
#endif

varying vec4 v_color;
varying vec2 v_texCoords;

uniform sampler2D u_texture;
uniform float u_time;
uniform vec2 u_resolution;

vec3 rgb2hsv(vec3 c)
{
    vec4 K = vec4(0.0, -1.0 / 3.0, 2.0 / 3.0, -1.0);
    vec4 p = mix(vec4(c.bg, K.wz), vec4(c.gb, K.xy), step(c.b, c.g));
    vec4 q = mix(vec4(p.xyw, c.r), vec4(c.r, p.yzx), step(p.x, c.r));

    float d = q.x - min(q.w, q.y);
    float e = 1.0e-10;
    return vec3(abs(q.z + (q.w - q.y) / (6.0 * d + e)), d / (q.x + e), q.x);
}

vec3 hsv2rgb(vec3 c)
{
    vec4 K = vec4(1.0, 2.0 / 3.0, 1.0 / 3.0, 3.0);
    vec3 p = abs(fract(c.xxx + K.xyz) * 6.0 - K.www);
    return c.z * mix(K.xxx, clamp(p - K.xxx, 0.0, 1.0), c.y);
}

void main() {
  vec4 c = v_color * texture2D(u_texture, v_texCoords);
  vec2 onePixel = vec2(1.0) / u_resolution;

  float intensity = (c.r + c.g + c.b) / 3.0;
  float time = u_time * 0.25;
  float hue = (sin(v_texCoords.x / 10.) * .5 + .5) * 360.0;
  hue = mod(hue + time, 360.0);

  intensity = sqrt(intensity) * 1.5;
  vec3 hsv = vec3(hue, .8, intensity);
  vec3 rgb = hsv2rgb(hsv);

  gl_FragColor = vec4(rgb.r, rgb.g, rgb.b, c.a);
}