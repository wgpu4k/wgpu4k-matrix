
2. Replace dst ?: Create() with default values
3. Vec4 and Quat should use floats and not doubles, then check for redundant toFLoats()
5. Add more tests
6. Check for conversion artifacts and references to the JS impl
7. Add dokka once dokkaV2 stabilizes
8. Later - inline classes