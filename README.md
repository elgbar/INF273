# INF273 Meta- Heuristics

This is a project done for [Meta- Heuristics](https://www.uib.no/en/course/INF273) at the university of Bergen during the spring of 2020.
This repo contains the final project shown during the exam of the course ([link to the release](https://github.com/kh498/INF273/releases/tag/exam)).

## Purpose of this project

During the course we were tasked to create software that would run meta-heuristic algorithms. Each assignment was built on top of the last assignment. This repo is the accumulated assignments and final exam of the course.

Each tab denotes how the project was after the denoted assignment. You can find the assignments in the [assignments folder](./assignments).

Concretely this is a meta-heuristic model to solve Vehicle Routing Problem with Time Windows. The lower the objective value the better the quality of the solution.  

## How to run

To run open the project in IntelliJ, then select a profile to run. Not all profiles works, but those with a higher assignment number (ie 4 or 5) are likely to work while those marked as `exam` are guaranteed to work. You can edit how the algorithm will run by tweaking the given parameters. Run the `help` configuration for more information (or just look in the source).

### Scores to beat

During the project the highest scoring solutions was release to the students. Sadly this project did not make it to any top spot (except for a very brief one time when (TODO upload picture of it)). If you for some reason want to try to beat the the class could manage see the table below.

#### This projects best scores

Note: During the exam we had a strict time limit for each instance, ie the time was the limiting factor.
Note 2: For `Call_7_Vehicle_3` an objective value of `1476444` the the global best solution.

| Instance            | Average Objective | Best Objective | Average running time |
|---------------------|-------------------|----------------|----------------------|
| Call_7_Vehicle_3    | 1476444           | 1476444        | 0.203 seconds        |
| Call_18_Vehicle_5   | 2464250           | 2432417        | 19.5 seconds         |
| Call_035_Vehicle_07 | 5575721           | 5483069        | 49.5 seconds         |
| Call_080_Vehicle_20 | 13966935          | 13464825       | 119.505 seconds      |
| Call_130_Vehicle_40 | 19614774          | 19203245       | 399.501 seconds      |

#### Best average (robustness) of whole class

| Instance            | Student    | Average Objective | Average running time |
|---------------------|------------|-------------------|----------------------|
| Call_7_Vehicle_3    | Student #3 | 1476444           | 0.035 seconds        |
| Call_18_Vehicle_5   | Student #1 | 2400016           | 19 seconds           |
| Call_035_Vehicle_07 | Student #1 | 4632116           | 49 seconds           |
| Call_080_Vehicle_20 | Student #1 | 11658679.9        | 119 seconds          |
| Call_130_Vehicle_40 | Student #1 | 17384351.3        | 299 seconds          |

#### Best-found solution of whole class

| Instance            | Student    | Best-found Objective | Running time (approx.) |
|---------------------|------------|----------------------|------------------------|
| Call_7_Vehicle_3    | Student #2 | 1476444              | 1.26 seconds           |
| Call_18_Vehicle_5   | Student #1 | 2400016              | 19 seconds             |
| Call_035_Vehicle_07 | Student #1 | 4580935              | 49 seconds             |
| Call_080_Vehicle_20 | Student #1 | 11543162             | 119 seconds            |
| Call_130_Vehicle_40 | Student #1 | 17270951             | 299 seconds            |

## Release of the project

This project was released after the exam and the grade was published. This project is considered finished and will not receive any future updates.

## License

This project is released to the public under the [The Unlicense](unlicense.org). The full length license can be found in the file [UNLICENSE](./UNLICENSE), and also on the unlicense website linked above.
