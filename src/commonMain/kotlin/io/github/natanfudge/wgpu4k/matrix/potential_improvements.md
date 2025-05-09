3. probably add premultiplication functions so it will be less nonsensical

1. Reorganize the files Mat3f, Mat4f, Quatf, Vec2f, Vec3f, Vec4f, to be ordered like this:
<secondary constructors>
<companion object>
    <constants>
    <static builders>
    <static functions>
</companion object>
<`operator fun` functions>
<functions with 0 parameters>
<functions with 1 parameter>
<functions with 2 parameters>
<functions with 3 or more parameters>

2. Remove references to javascript / typescript
3. Remove hanging comments that seem like arbitrarily placed notes
4. 

8. Check for conversion artifacts , references to the JS impl, llm notes, out-of-place comments, weird code, inconsistencies, consistent function ordering
7. Add dokka once dokkaV2 stabilizes
8. Later - inline classes