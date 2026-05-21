package com.jjjcfy.greatech.content.cover;

import java.util.Map;

import net.minecraft.core.Direction;

public interface GreatechCoverHost {
    boolean canInstallCover(Direction face);

    boolean installCover(Direction face, GreatechCoverType type);

    GreatechCoverState removeCover(Direction face);

    GreatechCoverState getCover(Direction face);

    Map<Direction, GreatechCoverState> covers();
}
