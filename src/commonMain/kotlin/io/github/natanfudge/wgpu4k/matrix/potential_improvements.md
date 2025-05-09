3. Add premulplication for mat4f
For the kdocs of mat4f, clarify for post multiplication that if you do M * v the transform applies before `this`, and 
if you do v * M the transform applies after `this`, and vice versa for premultiplication

1. Reorganize the files Mat3f, Mat4f, Quatf, Vec2f, Vec3f, Vec4f, to be ordered like this:
<secondary constructors>
<companion object>
    <constants>
    <static builders>
    <static operators>
</companion object>
<`operator fun` functions>
<properties>
<functions with 0 parameters>
<functions with 1 parameter>
<functions with 2 parameters>
<functions with 3 or more parameters>
<toString>
<equals>
<hashcode>

2. Remove references to javascript / typescript
3. Remove hanging comments that seem like arbitrarily placed notes
4. In cases arithmetic is used unnecessarily, like `array[1 * 4 + 1]`, compact the final number, to get like `array[5]`
5. Find weird or inconsistent code across Mat3f, Mat4f, Quatf, Vec2f, Vec3f, Vec4f
7. Add dokka once dokkaV2 stabilizes
8. Later - inline classes