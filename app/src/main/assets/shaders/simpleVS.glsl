uniform mat4 uMVPMatrix;
attribute vec3 aPos;
attribute vec3 aColor;

varying vec3 fColor;

void main()
{
    gl_Position = uMVPMatrix * vec4(aPos.x, aPos.y, aPos.z, 1.0);
    fColor = aColor;
}
