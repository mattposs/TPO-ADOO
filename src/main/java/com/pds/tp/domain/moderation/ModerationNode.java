package com.pds.tp.domain.moderation;

import com.pds.tp.domain.entity.Report;
import lombok.Setter;

/**
 * Chain of Responsibility node for report moderation.
 */
@Setter
public abstract class ModerationNode {
    protected ModerationNode next;

    public abstract void handle(Report report);

    protected void passToNext(Report report) {
        if (this.next != null) {
            this.next.handle(report);
        }
    }
}
