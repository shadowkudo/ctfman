# BDR presentations

- Constraints:
  - 15min
  - 7-10 slides should cover the following topics
    - The issue
    - Implementation choices
    - MCD
    - MLD
    - Implementation details
  - Demo
  - Questions for the application

> [!INFO]
> We need to push the slides to the repo by 13H monday and the release should be
> done on sunday and contain the previous phases up to date (meaning that the uml
> might need some fixing by then)

## Structure idea

- The issue
- Implementation choices
  - Talk about the multiple times we had to go back to the drawing board
  - MCD
    - The inheritance from authentication (because we needed it to be more complex)
  - MLD (generated ERD after writing the creation script by following the EA model)
  - Maybe take some time to talk about some triggers/views
- Implementation details
  - Procedure to simplify the complex insertion that came with the inheritance
    approach
  - Using views for shorter queries on the backend
  - Backend/Frontend separation
  - More details on the Model approach in the backend
  - `Maybe reuse slides from DAI`
- Demo
- Questions
