package tr.com.nkolay.itam.service.dashboard;

import tr.com.nkolay.itam.model.dto.DashboardSummaryResponse;

public interface DashboardService {

    /** @param expiringSoonDays bugünden itibaren kaç gün içinde bitecek lisansların sayılacağı */
    DashboardSummaryResponse getSummary(int expiringSoonDays);
}
