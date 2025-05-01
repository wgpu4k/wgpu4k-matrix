
3. Vec4 and Quat should use floats and not doubles, then check for redundant toFLoats()
   Change Vec4 and Quat to use floats instead of doubles. There will likely be conversion errors so use gradlew compiletestkotlinjvm to weed them out.
   Make sure to not change any logic, just refactor some toFloat/toDouble calls and change types from Double to FLoat
5. Add more tests
6. Check for conversion artifacts and references to the JS impl
7. Add dokka once dokkaV2 stabilizes
8. Later - inline classes