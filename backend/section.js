const rateLimit = require("express-rate-limit");

const NONE = rateLimit({
    windowMs: 1 * 60 * 1000,
    limit: 500, 
    standardHeaders: true, 
    legacyHeaders: false,
});
const LOW_STRENGTH = rateLimit({
    windowMs: 2 * 60 * 1000,
    limit: 50,
    standardHeaders: true,
    legacyHeaders: false,
});
const MEDIUM_STRENGTH = rateLimit({
    windowMs: 2 * 60 * 1000,
    limit: 20,
    standardHeaders: true,
    legacyHeaders: false,
});
const HIGH_STRENGTH = rateLimit({
    windowMs: 2 * 60 * 1000, 
    limit: 5, 
    standardHeaders: true,
    legacyHeaders: false,
});
const MAX_STRENGTH = rateLimit({
    windowMs: 2 * 60 * 1000,
    limit: 1,
    standardHeaders: true,
    legacyHeaders: false,
});
function CUSTOM(limit) {
    return rateLimit({
        windowMs: 2 * 60 * 1000, 
        limit: limit, 
        standardHeaders: true, 
        legacyHeaders: false, 
    });
}

const limits = {
    /** 300 attempts every 5 minutes */
    NONE,
    /** 100 attempts every 5 minutes */
    LOW_STRENGTH,
    /** 20 attempts every 5 minutes */
    MEDIUM_STRENGTH,
    /** 5 attempts every 5 minutes */
    HIGH_STRENGTH,
    /** 1 attempt every 5 minutes */
    MAX_STRENGTH,
    /** 
     * @param limit The number of attempts allowed every 5 minutes
     * @returns {Function} The rate limit middleware
     */
    CUSTOM
};

module.exports = {
    limits
};