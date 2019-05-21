uniform mat4 uMVPMatrix;
attribute vec3 aPos;
attribute vec2 aTexCoord;

varying vec2 fTexCoord;

void main()
{
    gl_Position = uMVPMatrix * vec4(aPos.x, aPos.y, aPos.z, 1.0);
    fTexCoord = aTexCoord;
}
