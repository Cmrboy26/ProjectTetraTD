attribute vec4 a_position;
attribute vec4 a_color;
attribute vec2 a_texCoord0;

uniform mat4 u_projTrans;
uniform vec2 u_resolution;

varying vec4 v_color;
varying vec2 v_texCoords;

void main() {
    v_color = a_color;
    vec2 texCoord = a_texCoord0;
    vec2 relativePos = a_position.xy / u_resolution - .5;

    texCoord.x += sin(relativePos.y) * .005;;
    //texCoord.y += sin(relativePos.y);

    v_texCoords = texCoord;
    gl_Position = u_projTrans * a_position;
}