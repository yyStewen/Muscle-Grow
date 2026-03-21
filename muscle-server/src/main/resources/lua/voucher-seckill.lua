local stockKey = KEYS[1]
local boughtKey = KEYS[2]
local pendingKey = KEYS[3]

local userId = ARGV[1]
local pendingTtlSeconds = tonumber(ARGV[2])

if redis.call('SISMEMBER', boughtKey, userId) == 1 then
    return 2
end

if redis.call('EXISTS', pendingKey) == 1 then
    return 3
end

local stock = tonumber(redis.call('GET', stockKey))
if not stock or stock <= 0 then
    return 1
end

redis.call('DECR', stockKey)
redis.call('SET', pendingKey, userId, 'EX', pendingTtlSeconds)
return 0
