package pamViewFX.threeD;

/*
 * Copyright (C) 2013-2015 F(X)yz, 
 * Sean Phillips, Jason Pollastrini and Jose Pereda
 * All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import javafx.scene.AmbientLight;
import javafx.scene.DepthTest;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;

/**
 *
 * @author Sean
 */
public class SurfacePlot extends Group {

	public AmbientLight selfLight = new AmbientLight(Color.WHITE);
	public double nodeRadius = 1;
	private double axesSize = 1000;
	private boolean normalized = false;
	public boolean selfLightEnabled = true;
	public Color color = Color.WHITE;
	private TriangleMesh mesh;
	public MeshView meshView;
	public PhongMaterial material;

	public SurfacePlot(boolean selfLit) {
		selfLightEnabled = selfLit;
		init();
	}

	public SurfacePlot(float[][] arrayY, int spacing, Color spectacular, Color diffuse, boolean fill, boolean selfLit) {
		selfLightEnabled = selfLit;
		init();
		setHeightData(arrayY,spacing, spectacular, diffuse, selfLit,fill);
	}

	private void init() {
		if (selfLightEnabled) {
			getChildren().add(selfLight);
		}
		setDepthTest(DepthTest.ENABLE);
	}

	public void setHeightData(float[][] arrayY, int spacing, Color spectacular, Color diffuse, boolean ambient, boolean fill) {
		material = new PhongMaterial();
		material.setSpecularColor(spectacular);
		material.setDiffuseColor(diffuse);


		mesh=createHeightMap( arrayY, 1,  1);

		//Create a viewable MeshView to be added to the scene
		//To add a TriangleMesh to a 3D scene you need a MeshView container object
		meshView = new MeshView(mesh);
		
//		//quick hack to get into another co-ordinate space....
//		meshView.setRotationAxis(new Point3D(1,0,0));
//		meshView.setRotate(-90);
		
		
		//The MeshView allows you to control how the TriangleMesh is rendered
		if(fill) { 
			meshView.setDrawMode(DrawMode.FILL);
		} else {
			meshView.setDrawMode(DrawMode.LINE); //show lines only by default
		}
		meshView.setCullFace(CullFace.NONE); //Removing culling to show back lines

		getChildren().add(meshView);
		meshView.setMaterial(material);
		if (ambient) {
			selfLight.getScope().add(meshView);
			if(!getChildren().contains(selfLight))
				getChildren().add(selfLight);
		}
		else if(getChildren().contains(selfLight))
			getChildren().remove(selfLight);
		setDepthTest(DepthTest.ENABLE);
	}


	/**
	 * Create a height map from an array. 
	 * @param arrayY
	 * @param pskip
	 * @param scale
	 * @return
	 */
	public static TriangleMesh createHeightMap(float[][] arrayY, int pskip, float scale) {

		float  minX = -(float) arrayY.length / 2;
		float maxX = (float) arrayY.length / 2;

		float minY = -(float) arrayY[0].length / 2;
		float maxY = (float) arrayY[0].length / 2;

		int subDivX = (int) arrayY.length / pskip;
		int subDivY = (int) arrayY[0].length / pskip;

		final int pointSize = 3;
		final int texCoordSize = 2;
		// 3 point indices and 3 texCoord indices per triangle
		final int faceSize = 6;
		int numDivX = subDivX + 1;
		int numVerts = (subDivY + 1) * numDivX;
		float points[] = new float[numVerts * pointSize];
		float texCoords[] = new float[numVerts * texCoordSize];
		int faceCount = subDivX * subDivY * 2;
		int faces[] = new int[faceCount * faceSize];

		// Create points and texCoords
		for (int y = 0; y <= subDivY; y++) {
			float currY = (float) y / subDivY;
			double fy = (1 - currY) * minY + currY * maxY;

			for (int x = 0; x <= subDivX; x++) {
				float currX = (float) x / subDivX;
				double fx = (1 - currX) * minX + currX * maxX;

				int index = y * numDivX * pointSize + (x * pointSize);
				points[index] = (float) fx * scale;   // x
				points[index + 2] = (float) fy * scale;  // y
				// color value for pixel at point
				//System.out.println(" x: "+ x +  " y: "+y +  " subDivX: "+subDivX+ " subDivY "+subDivY);
				
				if (x==arrayY.length || y==arrayY[0].length) points[index + 1]=0; //BUG- dinnae understand but stops weird lines on graph 
				else points[index + 1] = arrayY[x][y];

				index = y * numDivX * texCoordSize + (x * texCoordSize);
				texCoords[index] = currX;
				texCoords[index + 1] = currY;
			}
		}

		// Create faces
		for (int y = 0; y < subDivY; y++) {
			for (int x = 0; x < subDivX; x++) {
				int p00 = y * numDivX + x;
				int p01 = p00 + 1;
				int p10 = p00 + numDivX;
				int p11 = p10 + 1;
				int tc00 = y * numDivX + x;
				int tc01 = tc00 + 1;
				int tc10 = tc00 + numDivX;
				int tc11 = tc10 + 1;

				int index = (y * subDivX * faceSize + (x * faceSize)) * 2;
				faces[index + 0] = p00;
				faces[index + 1] = tc00;
				faces[index + 4] = p10;
				faces[index + 3] = tc10;
				faces[index + 2] = p11;
				faces[index + 5] = tc11;

				index += faceSize;
				faces[index + 0] = p11;
				faces[index + 1] = tc11;
				faces[index + 4] = p01;
				faces[index + 3] = tc01;
				faces[index + 2] = p00;
				faces[index + 5] = tc00;
			}
		}
		//to do
		//int smoothingGroups[] = new int[faces.length / faceSize];

		TriangleMesh mesh = new TriangleMesh();
		mesh.getPoints().addAll(points);
		mesh.getTexCoords().addAll(texCoords);
		mesh.getFaces().addAll(faces);
		//mesh.getFaceSmoothingGroups().addAll(smoothingGroups);

		return mesh;
	}



}