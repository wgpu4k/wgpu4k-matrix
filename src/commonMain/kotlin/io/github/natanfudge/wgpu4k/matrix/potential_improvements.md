2. Go over the transformation functions and check if they post-multiply, and document accordingly using gemini, example:
   /**
    * Post-multiplies this 3x3 matrix by a 2D translation and writes the result into[dst].
    * So if you would multiply a vector with the resulting matrix, the translation would apply first, and only then the original matrix's transform.
    *
    * */
2. Rewrite the tests given this new information.
3. probably add premultiplication functions so it will be less nonsensical
5. Add column-major as its own factory function, and make default row-major (?)
5. I think we don't want to have default values for the constructors, that will make easy mistakes. 
5. Add more tests
5.5. weird extra empty line at the start of every function
6. Check for conversion artifacts , references to the JS impl, llm notes, out-of-place comments, weird code, inconsistencies, consistent function ordering
7. Add dokka once dokkaV2 stabilizes
8. Later - inline classes