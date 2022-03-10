package dk.aau.cs.TCTL;

import dk.aau.cs.TCTL.visitors.ITCTLVisitor;

public class TCTLAtomicPropositionNode extends TCTLAbstractStateProperty {

	private TCTLAbstractStateProperty left;
	private TCTLAbstractStateProperty right;
	private String op;

	public TCTLAbstractStateProperty getLeft() {
		return left;
	}

	public void setLeft(TCTLAbstractStateProperty left) {
		this.left = left;
	}
	
	public TCTLAbstractStateProperty getRight() {
		return right;
	}

	public void setRight(TCTLAbstractStateProperty right) {
		this.right = right;
	}

	public String getOp() {
		return op;
	}

	public void setOp(String op) {
		this.op = op;
	}

	public TCTLAtomicPropositionNode(TCTLAbstractStateProperty left, String op, TCTLAbstractStateProperty right) {
		this.left = left;
		this.op = op;
		this.right = right;
	}
	
	@Override
	public TCTLAbstractStateProperty copy() {
		return new TCTLAtomicPropositionNode(left.copy(), op, right.copy());
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof TCTLAtomicPropositionNode) {
			TCTLAtomicPropositionNode node = (TCTLAtomicPropositionNode) o;
			// TODO: Not sure if this is intentional but this is reference
			// equals and not equality
			return left.equals(node.left)
					&& right.equals(node.right) && op.equals(node.getOp());
		}
		return false;
	}

	@Override
	public TCTLAbstractStateProperty replace(TCTLAbstractProperty object1,
			TCTLAbstractProperty object2) {
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
        left.convertForReducedNet(templateName);
        right.convertForReducedNet(templateName);
    }

    @Override
	public String toString() {
		return left + " " + op + " " + right;
	}

	@Override
	public void accept(ITCTLVisitor visitor, Object context) {
		visitor.visit(this, context);

	}

	@Override
	public boolean containsPlaceHolder() {
		return left.containsPlaceHolder() || right.containsPlaceHolder();
	}

    @Override
    public boolean hasNestedPathQuantifiers() {
        return false;
    }

/*    @Override
    public StringPosition[] getChildren() {
        StringPosition[] children = new StringPosition[2];

        int start = 0;
        int end = 0;
        boolean leftSimpleProperty = left.isSimpleProperty();

        start = leftSimpleProperty ? 0 : 1;
        end = start + left.toString().length();

        StringPosition posLeft = new StringPosition(start, end, left);

        start = end + 5 + (right.isSimpleProperty() ? 0 : 1)
            + (leftSimpleProperty ? 0 : 1);

        end = start + right.toString().length();

        StringPosition posRight = new StringPosition(start, end, right);

        children[0] = posLeft;
        children[1] = posRight;
        return children;
    }*/

    @Override
	public TCTLAbstractProperty findFirstPlaceHolder() {
		TCTLAbstractProperty rightP = right.findFirstPlaceHolder(); 
		
		return rightP == null ? left.findFirstPlaceHolder() : rightP;
	}

	public boolean containsAtomicPropositionWithSpecificPlaceInTemplate(String templateName, String placeName) {
		return right.containsAtomicPropositionWithSpecificPlaceInTemplate(templateName, placeName) ||
				left.containsAtomicPropositionWithSpecificPlaceInTemplate(templateName, placeName);
	}
	
	
	public boolean containsAtomicPropositionWithSpecificTransitionInTemplate(String templateName, String transitionName) {
		return right.containsAtomicPropositionWithSpecificTransitionInTemplate(templateName, transitionName) ||
				left.containsAtomicPropositionWithSpecificTransitionInTemplate(templateName, transitionName);
	}
}
