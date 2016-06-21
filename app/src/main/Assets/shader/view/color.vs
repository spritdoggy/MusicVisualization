uniform mat4 worldMat, viewMat, projMat, scaleMat;

attribute vec3 position;
attribute  vec3 normal;
attribute  vec2 texCoord;

varying vec3 v_normal;
varying vec2 v_texCoord;

void main() {
    gl_Position = projMat * viewMat * worldMat * scaleMat * vec4(position, 1.0);
    v_normal = normal;
    v_texCoord = texCoord;
}