package icu.azim.wagrapple.components;

import nerdhub.cardinal.components.api.component.Component;

public interface IGrappleComponent extends Component{
	boolean isGrappled();
	void setGrappled(boolean b);
	int getLineId();
	void setLineId(int id);
}
