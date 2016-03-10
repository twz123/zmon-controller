package de.zalando.zmon.security.tvtoken;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import de.zalando.zmon.persistence.OnetimeTokensSProcService;

public class TvTokenService {

    public static final String ZMON_TV_ID = "ZMON_TV_ID";

    public static final String ZMON_TV = "ZMON_TV";

    public static final String X_FORWARDED_FOR = "X-FORWARDED-FOR";

    private final OnetimeTokensSProcService oneTimeTokenSProcService;

    public TvTokenService(OnetimeTokensSProcService onetimeTokensSProcService) {
        this.oneTimeTokenSProcService = onetimeTokensSProcService;
    }

    public static String remoteIp(HttpServletRequest request) {
        if (request != null) {
            return request.getRemoteAddr();
        }
        return "UNKNOWN";
    }

    // TODO use sproc
    public boolean isValidToken(String token, String bindIp, String sessionId) {
        List<Integer> result = oneTimeTokenSProcService.bindOnetimeToken(token, bindIp, sessionId);
        return result.size() > 0 ? true : false;
    }


}
