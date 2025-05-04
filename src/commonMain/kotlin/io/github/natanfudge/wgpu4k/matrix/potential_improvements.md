4. Add column-major as its own factory function, and make default row-major (?)
5. I think we don't want to have default values for the constructors, that will make easy mistakes. 
5. Add more tests
6. Check for conversion artifacts , references to the JS impl, llm notes, out-of-place comments, weird code, inconsistencies
7. Add dokka once dokkaV2 stabilizes
8. Later - inline classes