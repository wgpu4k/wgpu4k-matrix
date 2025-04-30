1. Get rid of the shitty docs and write something more useful - just Vec4 remaining

Visit the source kotlin files in the project and update the kdocs according to this rule:

functions/constructors should rarely use @param. Rather, they should write the usage of the parameters inline using [] notation like is done in the standard library. Only use `@param` when the parameter doesn't fit in the main sentence describing what the function does. Do not mention the `dst` parameter. @return should only be used when returning a value is secondary to the function itself. For example, a getter should not have @return because that's the entirety of what a getter does. However, if a setter returns a value, that's special and should be specially documented with @return. If a function already says that its function is to give you some result x, do not repeat the fact that it returns x. For example, "Applies rounding to all elements, returns all elements rounded" can be shortened to "Rounds all elements". "Adds vector a to vector b, returns the sum of the vectors" can be shortened to "adds vector a to vector b"
refer to "vector 'this'" as  "`this`" (including the ` symbol)
Do not refer to things as "vector x" or "matrix x" etc, just refer to them by their name e.g. "[x]" (including the square brackets)
2. Replace dst ?: Create() with default values
3. Vec4 and Quat should use floats and not doubles, then check for redundant toFLoats()
5. Add more tests
6. Check for conversion artifacts and references to the JS impl
6. Later - inline classes