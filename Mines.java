import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class Mines {

    List<int[]> minesPos = new ArrayList<>(); // Contains the mines position coordinates read from the input file.
    int[] startPos = new int[2]; // Contains the start position coordinates read from the input file.
    int[] treasurePos = new int[2]; // Contains the treasure position coordinates read from the input file.

    Set<int[]> hull = new HashSet<>(); // Contains the points that define the Convex Hull.
    List<int[]> topSubHull = new ArrayList<>(); // Contains the points that define the top sub-hull of the Convex Hull.
    List<int[]> bottomSubHull = new ArrayList<>(); // Contains the points that define the bottom sub-hull of the Convex Hull.

    Mines(String inputFileDir) {
        // Check if loadFile() successfully read the specified input file.
        // If so, call findShortestPath().
        if (loadFile(inputFileDir)) {
            findShortestPath();
        }
    }

    /**
     * Method used to read the contents (point coordinates) of the specified
     * input file and store them into minePos.
     * @param dir Input file directory.
     * @return True if the specified input file was successfully loaded, False if an error occurred.
     */
    private boolean loadFile(String dir) {
        File file = new File(dir);
        Scanner scanner;
        String line;
        String[] coordinates;

        try {
            scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                line = scanner.nextLine();

                if (!line.equals("")) {
                    coordinates = line.split(" ");

                    int[] point = new int[2];
                    point[0] = Integer.parseInt(coordinates[0]);
                    point[1] = Integer.parseInt(coordinates[1]);

                    minesPos.add(point);
                }
            }
            this.startPos = minesPos.remove(0); // Remove the first element from minePos and set its value to startPos.
            this.treasurePos = minesPos.remove(0); // Remove the second element (now in the first element position, since
                                                     // we extracted the first element for startPos) from minePos and set
                                                     // its value to treasurePos.
            return true;
        } catch (FileNotFoundException e) {
            System.out.println("*** Error: File not Found. ***");
            return false;
        }
    }

    /**
     * Calculates the distance between 2 given points.
     * @param point1 The first point.
     * @param point2 The second point.
     * @return The distance between the 2 given points.
     */
    private double pointsDistance(int[] point1, int[] point2) {
        return Math.sqrt(Math.pow(point2[0] - point1[0], 2) + Math.pow(point2[1] - point1[1], 2));
    }

    /**
     * Given 2 points tp1 and tp2, the method returns the max point by
     * determining if the maximum angle is achieved by tp1p1pn or tp2p1pn.
     * @param tp1 The first given test point.
     * @param tp2 The second given test point.
     * @param p1 Point p1 of the given p1pn line.
     * @param pn Point pn of the given p1pn line.
     * @return The point (tp1 or tp2) that was determined to be the max point.
     */
    public int[] maxPointUsingAngle(int[] tp1, int[] tp2, int[] p1, int[] pn) {
        double p1pnDistance = pointsDistance(p1, pn);

        // Calculate the angle tp1p1pn for test point 1 (tp1)
        double tp1pnDistance = pointsDistance(tp1, pn);
        double tp1p1Distance = pointsDistance(tp1, p1);
        // Law of cosines
        double angle1 = Math.acos((Math.pow(tp1p1Distance, 2) + Math.pow(p1pnDistance, 2) - Math.pow(tp1pnDistance, 2))
                / (2 * tp1p1Distance * p1pnDistance));

        // Calculate the angle tp2p1pn for test point 2 (tp2)
        double tp2pnDistance = pointsDistance(tp2, pn);
        double tp2p1Distance = pointsDistance(tp2, p1);
        // Law of cosines
        double angle2 = Math.acos((Math.pow(tp2p1Distance, 2) + Math.pow(p1pnDistance, 2) - Math.pow(tp2pnDistance, 2))
                / (2 * tp2p1Distance * p1pnDistance));

        if (angle1 >= angle2) {
            return tp1;
        } else {
            return tp2;
        }
    }

    /**
     * Determines if a given point is located on the left side or on the right side of the line
     * formed by 2 other given points.
     * @param targetPoint The point for which we want to determine the location relative to the line side.
     * @param linePoint1 The first point defining the given line.
     * @param linePoint2 The second point defining the given line.
     * @return 1 if the given point is located to the left of the line, 0 if the point is located on the line,
     * -1 if the point is located to the right of the line.
     */
    private int determinePointSide(int[] targetPoint, int[] linePoint1, int[] linePoint2) {
        // Based on a formula from "Introduction to the Design and Analysis of Algorithms" by Levitin,
        // page 197
        int value = (targetPoint[1] - linePoint1[1]) * (linePoint2[0] - linePoint1[0]) -
                (linePoint2[1] - linePoint1[1]) * (targetPoint[0] - linePoint1[0]);

        if (value > 0) {
            return 1;
        } else if (value < 0) {
            return -1;
        } else {
            return 0;
        }
    }

    /**
     * Calculates the distance of a given point from a given line.
     * @param targetPoint The given point for which we want to calculate the distance from a line.
     * @param linePoint1 The first point defining the given line.
     * @param linePoint2 The second point defining the given line.
     * @return The distance of the given point from the given line.
     */
    private int pointToLineDist(int[] targetPoint, int[] linePoint1, int[] linePoint2) {
        // Based on a formula from "Introduction to the Design and Analysis of Algorithms" by Levitin,
        // page 197
        return Math.abs((targetPoint[1] - linePoint1[1]) * (linePoint2[0] - linePoint1[0]) -
                (linePoint2[1] - linePoint1[1]) * (targetPoint[0] - linePoint1[0]));
    }

    /**
     * Recursive method used to find the points that define the
     * Convex-Hull of a given 2D point space using the Quick Hull
     * algorithm.
     * @param p1 The first point that defines the base line used by the Quick Hull algorithm.
     * @param pn The second point that defines the base line used by the Quick Hull algorithm.
     * @param side Defines the side of the line on which the Quick Hull algorithm will expand
     *             the Hull. If set to 1, the sub-hull on the left side of line p1pn is expanded.
     *             If set to -1, the the sub-hull on the right side of line p1pn is expanded.
     * @param candidatePoints The given 2D point space.
     */
    private void quickHull(int[] p1, int[] pn, int side, List<int[]> candidatePoints) {
        int maxIndex = -1;
        int maxDistance = -1;

        List<int[]> newCandidatePoints = new ArrayList<>();

        //Find the point with the maximum distance from line p1pn
        for (int i = 0; i < candidatePoints.size(); i++) {
            int tempDistance = pointToLineDist(candidatePoints.get(i), p1, pn);
            if (determinePointSide(candidatePoints.get(i), p1, pn) == side) {
                // Since the point is on the correct side, add it to the new candidate points list.
                newCandidatePoints.add(candidatePoints.get(i));
                if (tempDistance > maxDistance) {
                // If the current mine point has a greater distance from p1pn than maxDistance
                    maxIndex = i;
                    maxDistance = tempDistance;
                } else if (tempDistance == maxDistance) {
                    // Find the index of the maximum point based on the angle maximization technique.
                    if (maxPointUsingAngle(candidatePoints.get(maxIndex), candidatePoints.get(i), p1, pn) ==
                            candidatePoints.get(i)) {
                        maxIndex = i;
                        maxDistance = tempDistance;
                    }
                }
            }
        }

        if (maxIndex == -1) {
            //If no points are found, add p1 and pn to the hull
            hull.add(p1);
            hull.add(pn);
            return;
        }

        quickHull(candidatePoints.get(maxIndex), p1, -determinePointSide(pn, candidatePoints.get(maxIndex), p1), newCandidatePoints);
        quickHull(candidatePoints.get(maxIndex), pn, -determinePointSide(p1, candidatePoints.get(maxIndex), pn), newCandidatePoints);
    }

    /**
     * Calls quickhull() to calculate the hull that consists of the
     * top sub-hull and the bottom sub-hull defined by the line between
     * the starting position and the treasure position.
     */
    private void findConvexHull() {
        // Clone minePos list property and pass separate clone copies to each one of
        // the quickHull() method calls.
        quickHull(startPos, treasurePos, 1, new ArrayList<>(minesPos));
        quickHull(startPos, treasurePos, -1, new ArrayList<>(minesPos));
    }

    /**
     * Groups the points of the calculated hull into sub-hulls, by adding the points
     * that belong to the top sub-hull to the topSubHull list property and the points
     * that belong to the bottom sub-hull to the bottomSubHull list property.
     */
    private void findSubHulls() {
        for (int[] point : hull) {
            int side = determinePointSide(point, startPos, treasurePos);
            if (side == 1) {
                topSubHull.add(point);
            } else if (side == -1) {
                bottomSubHull.add(point);
            } else {
                // If the point is located on the line connecting
                // startPos and treasurePos, add it to both sub-hulls.
                topSubHull.add(point);
                bottomSubHull.add(point);
            }
        }
    }

    /**
     * Helper function of mergeSort() used to merge the generated
     * sub-lists into a unified sorted list. Points are sorted into the
     * unified list based on their x-coordinate value.
     * @param subList1 The first sub-list.
     * @param subList2 The second sub-list.
     * @return A unified and sorted list that contains the elements of subList1 and subList2.
     */
    private List<int[]> merge(List<int[]> subList1, List<int[]> subList2) {
        int i = 0;
        int j = 0;

        List<int[]> mergedList = new ArrayList<>();

        int size1 = subList1.size();
        int size2 = subList2.size();
        while (i < size1 && j < size2) {
            // Compare points based on their x value
            if (subList1.get(i)[0] < subList2.get(j)[0]) {
                mergedList.add(subList1.get(i));
                i += 1;
            } else if (subList1.get(i)[0] > subList2.get(j)[0]){
                mergedList.add(subList2.get(j));
                j += 1;
            } else {
                // If the 2 points have an equal x-coordinate,
                // compare their y-coordinate
                if (subList1.get(i)[1] <= subList2.get(j)[1]) {
                    mergedList.add(subList1.get(i));
                    i += 1;
                } else if (subList1.get(i)[1] > subList2.get(j)[1]) {
                    mergedList.add(subList2.get(j));
                    j += 1;
                }
            }
        }

        if (i == size1) {
            while (j < size2) {
                mergedList.add(subList2.get(j));
                j += 1;
            }
        } else {
            while (i < size1) {
                mergedList.add(subList1.get(i));
                i += 1;
            }
        }

        return mergedList;
    }

    /**
     * Performs a merge sort on a given list of points.
     * @param initialList The given list of points.
     * @return A sorted list of points.
     */
    private List<int[]> mergeSort(List<int[]> initialList) {
        double size = initialList.size();

        if (size > 1) {
            List<int[]> subList1 = new ArrayList<>();
            for (int i = 0; i < Math.floor(size / 2); i++) {
                subList1.add(initialList.get(i));
            }

            List<int[]> subList2 = new ArrayList<>();
            for (int i = (int) Math.floor(size / 2); i < size; i++) {
                subList2.add(initialList.get(i));
            }

            subList1 = mergeSort(subList1);
            subList2 = mergeSort(subList2);
            initialList = merge(subList1, subList2);
        }

        return initialList;
    }

    /**
     * Calculates the total distance of a path, given a list containing
     * the points that define the path.
     * @param pathPoints A list of points that define the given path.
     * @return The total distance covered by the path.
     */
    private double calculatePathDistance(List<int[]> pathPoints) {
        double totalDistance = 0;

        for (int i = 0; i < pathPoints.size() - 1; i++) {
            totalDistance += pointsDistance(pathPoints.get(i), pathPoints.get(i + 1));
        }

        return totalDistance;
    }

    /**
     * Given a list of path points, generates the result path string in the format
     * specified by the task's description
     * @param pathPoints A list of path points.
     * @return A string that contains the result path in the appropriate format.
     */
    private String pathPointsString(List<int[]> pathPoints) {
        StringBuilder stringBuilder = new StringBuilder();

        int size = pathPoints.size();
        for (int i = 0; i < size - 1; i++) {
            stringBuilder.append(String.format("(%d,%d)-->", pathPoints.get(i)[0], pathPoints.get(i)[1]));
        }
        stringBuilder.append(String.format("(%d,%d)", pathPoints.get(size - 1)[0], pathPoints.get(size - 1)[1]));

        return stringBuilder.toString();
    }

    /**
     * Prints the given result path distance and path points in the appropriate
     * format specified by the task's description.
     * @param pathDistance The result path distance.
     * @param pathPoints The result path points
     */
    private void printResults(double pathDistance, List<int[]> pathPoints) {
        System.out.printf("The shortest distance is %.5f\n", pathDistance);
        System.out.println("The shortest path is:" + pathPointsString(pathPoints));
    }

    /**
     * Called to begin the calculations that will result
     * in the minefield problem solution based on a Convex Hull
     * Analysis solution.
     */
    public void findShortestPath() {
        findConvexHull();
        findSubHulls();

        List<int[]> topSortedPath = mergeSort(topSubHull);
        List<int[]> bottomSortedPath = mergeSort(bottomSubHull);

        double topPathDistance = calculatePathDistance(topSortedPath);
        double bottomPathDistance = calculatePathDistance(bottomSortedPath);

        if (topPathDistance <= bottomPathDistance) {
            printResults(topPathDistance, topSortedPath);
        } else {
            printResults(bottomPathDistance, bottomSortedPath);
        }
    }

    public static void main(String[] args) {
        Mines mines = new Mines(args[0]);
    }
}