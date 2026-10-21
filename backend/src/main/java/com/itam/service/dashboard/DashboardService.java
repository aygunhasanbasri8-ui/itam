package com.itam.service.dashboard;

import com.itam.model.dto.DashboardSummaryResponse;

public interface DashboardService {

    /** @param expiringSoonDays bugünden itibaren kaç gün içinde bitecek lisansların sayılacağı */
    DashboardSummaryResponse getSummary(int expiringSoonDays);
}
