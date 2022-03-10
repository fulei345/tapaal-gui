package dk.aau.cs.TCTL;

import dk.aau.cs.TCTL.visitors.ITCTLVisitor;

public class TCTLTrueNode extends TCTLAbstractStateProperty {

	public TCTLAbstractStateProperty copy() {
		return new TCTLTrueNode();
	}

	public TCTLAbstractStateProperty replace(TCTLAbstractProperty object1, TCTLAbstractProperty object2) {
		if (this == object1 && object2 instanceof TCTLAbstractStateProperty) {
			TCTLAbstractStateProperty obj2 = (TCTLAbstractStateProperty) object2;
			obj2.setParent(parent);
			return obj2;
		} else {
			return this;
		}
	}

    @Override
    public void convertForReducedNet(String templateName) {
    }

    @Override
	public void accept(ITCTLVisitor visitor, Object context) {
		visitor.visit(this, context);
	}

	public boolean containsAtomicPropositionWithSpecificPlaceInTemplate(String templateName, String placeName) {
		return false;
	}
	
	public boolean containsAtomicPropositionWithSpecificTransitionInTemplate(String templateName, String transitionName) {
		return false;
	}

	public boolean containsPlaceHolder() {
		return false;
	}

    @Override
    public boolean hasNestedPathQuantifiers() {
        return false;
    }

    public TCTLAbstractProperty findFirstPlaceHolder() {
		return null;
	}
	
	public boolean equals(Object obj) {
		if (obj instanceof TCTLTrueNode) {
			return true;
		}
		return false;
	}
	
	public String toString() {
		return "true";
	}
	
	public boolean isSimple() {
		return true;
	}

}
