package dk.aau.cs.TCTL;

import java.util.ArrayList;
import java.util.List;

import dk.aau.cs.TCTL.visitors.ITCTLVisitor;

public class TCTLAndListNode extends TCTLAbstractStateProperty {

	private List<TCTLAbstractStateProperty> properties;

	public void setProperties(List<TCTLAbstractStateProperty> properties) {
		this.properties = properties;

		for (TCTLAbstractStateProperty p : properties) {
			p.setParent(this);
		}
	}

	public List<TCTLAbstractStateProperty> getProperties() {
		return properties;
	}

	public TCTLAndListNode(List<TCTLAbstractStateProperty> properties) {
		this.properties = properties;

		for (TCTLAbstractStateProperty p : properties) {
			p.setParent(this);
		}
	}

	public TCTLAndListNode(TCTLAbstractStateProperty property1,
			TCTLAbstractStateProperty property2) {
		properties = new ArrayList<TCTLAbstractStateProperty>();

		addConjunct(property1);
		addConjunct(property2);
	}

	public TCTLAndListNode(TCTLAndListNode andListNode) {
		properties = new ArrayList<TCTLAbstractStateProperty>();

		for (TCTLAbstractStateProperty p : andListNode.properties) {
			addConjunct(p.copy());
		}
	}

	public TCTLAndListNode() {
		properties = new ArrayList<TCTLAbstractStateProperty>();
		TCTLStatePlaceHolder ph = new TCTLStatePlaceHolder();
		ph.setParent(this);
		properties.add(ph);
		ph = new TCTLStatePlaceHolder();
		ph.setParent(this);
		properties.add(ph);
	}

	public void addConjunct(TCTLAbstractStateProperty conjunct) {
		conjunct.setParent(this);
		properties.add(conjunct);
	}

	@Override
	public boolean isSimpleProperty() {
		return false;
	}

	@Override
	public String toString() {
		StringBuilder s = new StringBuilder();

		boolean firstTime = true;

		for (TCTLAbstractStateProperty prop : properties) {

			if (!firstTime) {
				s.append(" and ");
			}

			s.append(prop.isSimpleProperty() ? prop.toString() : "("
					+ prop.toString() + ")");
			firstTime = false;
		}

		return s.toString();
	}

	@Override
	public StringPosition[] getChildren() {
		StringPosition[] children = new StringPosition[properties.size()];

		int i = 0;
		int endPrev = 0;
		boolean wasPrevSimple = false;
		for (TCTLAbstractStateProperty p : properties) {

			int start = 0;
			int end = 0;

			if (i == 0) {
				wasPrevSimple = p.isSimpleProperty();
				start = wasPrevSimple ? 0 : 1;
				end = start + p.toString().length();

				endPrev = end;

			} else {
				start = endPrev + 5 + (p.isSimpleProperty() ? 0 : 1)
						+ (wasPrevSimple ? 0 : 1);
				end = start + p.toString().length();

				endPrev = end;
				wasPrevSimple = p.isSimpleProperty();
			}

			StringPosition pos = new StringPosition(start, end, p);

			children[i] = pos;
			i++;
		}

		return children;
	}

    @Override
    public void convertForReducedNet(String templateName) {
        for (TCTLAbstractProperty property : properties){
            property.convertForReducedNet(templateName);
        }
    }

    @Override
	public boolean equals(Object o) {
		if (o instanceof TCTLAndListNode) {
			TCTLAndListNode node = (TCTLAndListNode) o;
			return properties.equals(node.properties);
		}
		return false;
	}

	@Override
	public TCTLAbstractStateProperty copy() {
		ArrayList<TCTLAbstractStateProperty> copy = new ArrayList<TCTLAbstractStateProperty>();

		for (TCTLAbstractStateProperty p : properties) {
			copy.add(p.copy());
		}

		return new TCTLAndListNode(copy);
	}

	@Override
	public TCTLAbstractStateProperty replace(TCTLAbstractProperty object1,
			TCTLAbstractProperty object2) {
		if (this == object1 && object2 instanceof TCTLAbstractStateProperty) {
			TCTLAbstractStateProperty obj2 = (TCTLAbstractStateProperty) object2;
			obj2.setParent(parent);
			return obj2;
		} else {
			for (int i = 0; i < properties.size(); i++) {
				properties.set(i, properties.get(i).replace(object1, object2));
			}
			return this;
		}
	}

	@Override
	public void accept(ITCTLVisitor visitor, Object context) {
		visitor.visit(this, context);

	}
	
	public boolean containsAtomicPropositionWithSpecificPlaceInTemplate(String templateName, String placeName) {
		boolean atomicPropFound = false;

		for (TCTLAbstractStateProperty p : properties) {
			atomicPropFound = atomicPropFound || p.containsAtomicPropositionWithSpecificPlaceInTemplate(templateName, placeName);
		}

		return atomicPropFound;
	}
	
	public boolean containsAtomicPropositionWithSpecificTransitionInTemplate(String templateName, String transitionName) {
		boolean atomicPropFound = false;

		for (TCTLAbstractStateProperty p : properties) {
			atomicPropFound = atomicPropFound || p.containsAtomicPropositionWithSpecificTransitionInTemplate(templateName, transitionName);
		}

		return atomicPropFound;
	}

	@Override
	public boolean containsPlaceHolder() {
		boolean placeHolderFound = false;

		for (TCTLAbstractStateProperty p : properties) {
			placeHolderFound = placeHolderFound || p.containsPlaceHolder();
		}

		return placeHolderFound;
	}

    @Override
    public boolean hasNestedPathQuantifiers() {
        boolean foundNestedQuantifier = false;

        for (TCTLAbstractStateProperty p : properties) {
            foundNestedQuantifier = foundNestedQuantifier || p instanceof TCTLPathToStateConverter || p.hasNestedPathQuantifiers();
            if(foundNestedQuantifier){
                break;
            }
        }
        return foundNestedQuantifier;
    }

    @Override
	public TCTLAbstractProperty findFirstPlaceHolder() {
		TCTLAbstractProperty ph = null;
		for (TCTLAbstractStateProperty p : properties) {
			if (p.containsPlaceHolder()) {
				ph = p.findFirstPlaceHolder();
				break;
			}

		}
		return ph;
	}

}
