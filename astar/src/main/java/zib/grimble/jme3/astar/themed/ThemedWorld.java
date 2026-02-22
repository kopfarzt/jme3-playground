package zib.grimble.jme3.astar.themed;

import java.util.logging.Logger;

import com.google.common.graph.EndpointPair;
import com.jme3.asset.AssetManager;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.material.Material;
import com.jme3.material.RenderState.FaceCullMode;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Node;

import zib.grimble.jme3.astar.geonodes.GeoWorld;

public class ThemedWorld extends GeoWorld<ThemedNode, ThemedConnection> {
	private static final Logger LOG = Logger.getLogger(ThemedWorld.class.getName());
	private AssetManager assetManager;
	private Node worldNode;
	private Node nodesNode;
	private Node textsNode;
	private Node connectionsNode;
	private BitmapFont defaultFont = null;

	private ThemedNode startNode = null;
	private ThemedNode endNode = null;

	public ThemedWorld(AssetManager assetManager) {
		super();
		this.assetManager = assetManager;
	}

	public Node createWorldNode() {
		worldNode = new Node("ThemedWorld");

		nodesNode = new Node("ThemesNodes");
		textsNode = new Node("Texts");

		for (ThemedNode node : graph.nodes()) {
			nodesNode.attachChild(node.getGeometry());
			textsNode.attachChild(node.getTextNode());
		}

		connectionsNode = new Node("ThemedConnections");

		for(EndpointPair<ThemedNode> ep : graph.edges()) {
			ThemedConnection connection = graph.edgeValue(ep).get();
			connectionsNode.attachChild(connection.getGeometry());
		}

		worldNode.attachChild(nodesNode);
		worldNode.attachChild(connectionsNode);

		return worldNode;
	}

	public Node createWorldGuiNode() {
		textsNode = new Node("Texts");

		for (ThemedNode node : graph.nodes()) {
			textsNode.attachChild(node.getTextNode());
		}
		return textsNode;
	}

	public Material createShadedMaterial(ColorRGBA color) {
		Material material = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
		material.setBoolean("UseMaterialColors", true);
		material.setColor("Ambient", color);
		material.setColor("Diffuse", color);
		material.setColor("Specular", color);
		material.setFloat("Shininess", 64);
		material.getAdditionalRenderState().setFaceCullMode(FaceCullMode.Off);
		return material;
	}

	public BitmapText createText(String text) {
		BitmapFont font = getDefaultFont();
		BitmapText t = new BitmapText(font);
		t.setSize(font.getCharSet().getRenderedSize());
		t.setText(text);
		return t;
	}

	/**
	 * Lazy get cached default font.
	 * 
	 * @return
	 */
	public BitmapFont getDefaultFont() {
		if(defaultFont == null) {
			defaultFont = assetManager.loadFont("Interface/Fonts/Default.fnt");
		}
		return defaultFont;
	}

	public Node getNodesNode() {
		return nodesNode;
	}

	public Node getConnectionsNode() {
		return connectionsNode;
	}

	@Override
	public String toString() {
		return String.format("ThemedWorld [graph]", graph);
	}

	public void setAllIdle() {
		for (ThemedNode n : graph.nodes()) {
			n.setStatus(NodeStatus.IDLE);
		}
		for(EndpointPair<ThemedNode> ep : graph.edges()) {
			ThemedConnection conn = graph.edgeValue(ep).get();
			conn.setStatus(ConnectionStatus.IDLE);
		}
	}

	public void selectStartEndNode(ThemedNode node) {
		if(node != null) {
			if(startNode == null) {
				startNode = node;
			} else if(endNode == null) {
				if(node == startNode) {
					startNode = null;
				}
				else {
					endNode = node;
				}
			} else {
				if(node == endNode) {
					endNode = null;
				}
				else {
					startNode = endNode;
					endNode = node;
				}
			}

			setAllIdle();

			if(endNode != null) {
				endNode.setStatus(NodeStatus.END);
			}

			if(startNode != null) {
				startNode.setStatus(NodeStatus.START);
			}
		}
	}

	public void findPath() {
		findPath(startNode, endNode);
	}

	public void findPath(ThemedNode startNode, ThemedNode endNode) {
		resetPath();

		if(startNode != null && endNode != null) {

			if(findPathByAStar(startNode, endNode)) {

				walkPath(startNode, endNode,
						n -> {
							if(n == startNode) {
								n.setStatus(NodeStatus.START);
							}
							else if (n == endNode) {
								n.setStatus(NodeStatus.END);
							}
							else {
								n.setStatus(NodeStatus.PATH);
							}
						},
						c -> {
							((ThemedConnection)c).setStatus(ConnectionStatus.ACTIVE);
						});
			}
		}
	}

	public void connect(ThemedNode u, ThemedNode...vs) {
		for (ThemedNode n : vs) {
			getGraph().putEdgeValue(u, n, new ThemedConnection(u, n, this));
		}
	}
}

