package se.curvysoft.eve.tools.model.esi;

import lombok.Data;

import java.io.Serializable;

@Data
public class SharedData implements Serializable {
    private static final long serialVersionUID = 5181473925768966850L;
    private String currentPath;
}
