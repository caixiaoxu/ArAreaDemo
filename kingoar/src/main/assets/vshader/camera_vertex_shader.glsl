attribute vec4 a_Position;
attribute vec2 a_Coordinates;

uniform mat4 u_Matrix;
uniform mat4 u_TextureMatrix;

varying vec2 v_Coordinates;

void main() {
    gl_Position = u_Matrix * a_Position;
    v_Coordinates = (u_TextureMatrix * vec4(a_Coordinates, 1.0, 1.0)).xy;
}
