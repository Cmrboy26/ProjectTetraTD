#ifdef GL_ES
    precision mediump float;
#endif

varying vec4 v_color;
varying vec2 v_texCoords;
uniform sampler2D u_texture;
uniform vec2 u_resolution;
uniform float u_time;

void main() {
    vec2 relativePosition = gl_FragCoord.xy / u_resolution - .5;
    vec2 uv = v_texCoords;
    //float intensity = sin(u_time * 3.14 * 1. + relativePosition.y * 500.) * .5 + .5;
    float intensity = (sin(u_time * 3.14 * .25 + relativePosition.y * 5.) * .5 + .5);
    uv.y += intensity * .0005;
    vec4 color = texture2D(u_texture, uv) * v_color;
    color.rgb += intensity * 1.;
    color.rgb *= .15;

    /*
    float distance = length(relativePosition);
    float vignette = smoothstep(.3, .2, distance);
    color.rgb = mix(color.rgb, color.rgb * vignette, .6);
    color.rgb = mix(color.rgb, color.rgb * intensity, .6);
    */
    gl_FragColor = color;
}