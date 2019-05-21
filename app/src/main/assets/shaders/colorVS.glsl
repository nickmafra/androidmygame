uniform mat4 uMVPMatrix;

void main()
{
    gl_Position = uMVPMatrix * vec4(aPos.x, aPos.y, aPos.z, 1.0);
}
