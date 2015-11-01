package alg2;

import edu.princeton.cs.introcs.Picture;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

/*************************************************************************
 * This class will use a union find algorithm on an image.
 * It will return the amount of components
 * A binarized version of the image.
 * The Image with random colors .
 * 
 * @author Peter Ironside
 *************************************************************************/
public class ConnectedComponentImage {

	private Picture picture;
	private int[][] labeledPic;
	private int[] linked;
	private int threshold = 128;
	private int count = 0; // number of component
	private int height, width;
	private Color background = Color.white;// of the binarised picture
	private HashSet<Integer> identifiedComponents;

	public static void main(String[] agrs) {
		ConnectedComponentImage a = new ConnectedComponentImage(
				"images/shapes.bmp");
		a.getPicture().show();
		a.binaryComponentImage().show();
		a.colourComponentImage().show();
		System.out.println(a.countComponents() + " components");
	}

	public int[][] getPic() {
		return labeledPic;
	}

	public int[] getLabels() {
		return linked;
	}

	/**
	 * Constructor, to initialise fields
	 * 
	 * @param fileLocation
	 */
	public ConnectedComponentImage(String fileLocation) {
		picture = new Picture(fileLocation);
		height = picture.height();
		width = picture.width();
		linked = new int[picture.width() * picture.height()];
		identifiedComponents = new HashSet<Integer>();
		scan();
	}

	public void scan() {
		Picture p = binaryComponentImage(); // get the binarized version

		int[][] labels = new int[picture.width()][picture.height()];
		int nextLabel = 1;

		// First pass
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				Color colorOfPixel = p.get(x, y);
				if (!colorOfPixel.equals(background)) {

					// Labels of neighbors
					ArrayList<Integer> neighbors = new ArrayList<Integer>();
					for (int nx = -1; nx <= 1; nx++) {
						for (int ny = -1; ny <= 1; ny++) {

							// Check if the point(x + nx, y + ny) would go out
							// of the picture's boundary
							if ((x + nx < 0) || (y + ny < 0)
									|| (x + nx > picture.width() - 1)
									|| (y + ny > picture.height() - 1)) {
								continue; // if out of boundary, stop at this
											// stage and go to the next
											// iteration of the loop

							} else {

								if (p.get(x + nx, y + ny).equals(background)) {
									continue; // if the point is background,
												// skip
								}
								if (labels[x + nx][y + ny] != 0) {
									neighbors.add(labels[x + nx][y + ny]);
								}
							}
						}
					}

					if (neighbors.size() == 0) {
						// this is a new isolated pixel/new component => give it
						// the label,
						// then increment the label and count
						labels[x][y] = nextLabel;
						linked[nextLabel] = nextLabel;

						nextLabel++;
						count++;
					} else {

						int smallestNeighborLabel = neighbors.get(0);
						for (int neighbor : neighbors) {
							if (neighbor < labels[x][y]) {
								smallestNeighborLabel = neighbor;
							}
						}
						labels[x][y] = smallestNeighborLabel;

						// Union all found neighbor pixels
						for (int neighbor : neighbors) {
							union(neighbor,smallestNeighborLabel);
						}
					}
				}
			}
		}

		// Second pass, note linked[] is used in union method
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				int currentLabel = labels[x][y];
				if (currentLabel != 0) {
					int root = root(currentLabel);
					labels[x][y] = root;
					linked[currentLabel] = root;
					identifiedComponents.add(root);
				}
			}
		}

		labeledPic = labels;
	}

	private int root(int i) {
		int root = i;
		while (root != linked[root])
			root = linked[root];
		return root;
	}

	private boolean connected(int p, int q) {
		return root(p) == root(q);
	}

	// root of p becomes the root of q => p "gone"
	private void union(int p, int q) {

		// if already connected (same root) -> return/stop
		if (connected(p, q))
			return;

		int rP = root(p);
		int rQ = root(q);
		linked[rP] = rQ;

		// two components are grouped into one
		count--;
	}

	/**
	 * Returns the number of components identified in the image.
	 * 
	 * @return the number of components (between 1 and N)
	 */
	public int countComponents() {
		return count;
	}

	/**
	 * Returns the original image with each object bounded by a red box.
	 * 
	 * @return a picture object with all components surrounded by a red box
	 */
	public Picture identifyComonentImage() {
	return null;
	}

	/**
	 * Returns a picture with each object updated to a random colour.
	 * 
	 * @return a picture object with all components coloured.
	 */
	public Picture colourComponentImage() {
		Picture coloredPicture = binaryComponentImage();

		Random randomGenerator = new Random();
		Color[] colors = new Color[] { Color.blue, Color.cyan, Color.green,
				Color.darkGray, Color.magenta, Color.orange, Color.pink };

		HashMap<Integer, Color> labelColors = new HashMap<Integer, Color>();
		
		//give every component a random color
		Iterator<Integer> goThroughComponents = identifiedComponents.iterator();
		while (goThroughComponents.hasNext()) {
			Color newColor = colors[randomGenerator.nextInt(colors.length)];
			labelColors.put(goThroughComponents.next(), newColor);
		}

		//set the color
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				int label = labeledPic[x][y];
				
				if (label != 0) {
					//use the label as the key to get the defined color in the hashmap 
					Color toColor = labelColors.get(label);
					coloredPicture.set(x, y, toColor);
				}
			}
		}
		return coloredPicture;

	}

	public Picture getPicture() {
		return picture;
	}

	/**
	 * Returns a binarised version of the original image
	 * 
	 * @return a picture object with all components surrounded by a red box
	 */
	public Picture binaryComponentImage() {
		// Make a deep copy
		Picture binaraiszedPicture = new Picture(picture);

		for (int x = 0; x < binaraiszedPicture.width(); x++) {
			for (int y = 0; y < binaraiszedPicture.height(); y++) {
				Color c = binaraiszedPicture.get(x, y);
				if (Luminance.lum(c) < threshold) {
					binaraiszedPicture.set(x, y, Color.BLACK);
				} else {
					binaraiszedPicture.set(x, y, Color.WHITE);
				}

			}
		}
		return binaraiszedPicture;
	}
}
