package com.leaf.yaml.api;

import java.util.Map;

import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Represent;

import com.leaf.Leaf;


/**
 * Needed to register a custom tag.
 *
 * @param <T>
 */
public abstract class RepresentedClass<T> implements Represent {

	public String tag;

	/**
	 * Prepare a value for serialization.
	 * 
	 * @param data
	 *            the data to serialize
	 * @return a serialized map
	 */
	public abstract Map<String, Object> represent(T data);

	@SuppressWarnings("unchecked")
	@Override
	public Node representData(Object data) {
		return Leaf.getInstance().getRepresenter().representMapping(new Tag("!" + this.tag), represent((T) data));
	}
}