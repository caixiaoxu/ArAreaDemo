precision mediump float;
uniform vec4 u_Color;

void main() {
    float xDistance = 0.5 - gl_PointCoord.x;
    float yDistance = 0.5 - gl_PointCoord.y;
    float distanceFromCenter = sqrt(xDistance * xDistance + yDistance * yDistance);
    if(distanceFromCenter > 0.5){
        discard;
    } else {
        gl_FragColor = u_Color;
    }
}
