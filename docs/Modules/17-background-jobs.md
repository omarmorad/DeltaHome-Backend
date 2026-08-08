# Delta Homes — 17 · Stage 16 · Background Jobs

> **Stage 16.** Asynchronous job processing for scheduled tasks.

**Status:** Aspirational · **Dependencies:** Stage 12+ · **Effort:** M

---

## 1. Scope (Future)

- Scheduled broadcast sending
- Email/SMS queue processing
- Cleanup jobs (expired OTPs, sessions)

---

## 2. Implementation Options

### Option A: Spring Scheduling

```java
@Service
public class ScheduledJobService {
    
    @Scheduled(cron = "0 0 * * * *") // Every hour
    public void processBroadcastQueue() {
        // Process pending broadcasts
    }
    
    @Scheduled(cron = "0 0 * * * *") // Every hour
    public void cleanupExpiredOtps() {
        otpCodeRepository.deleteExpired();
    }
}
```

### Option B: Spring Batch

For more complex batch processing of large datasets.

---

## 3. Job Types

| Job | Schedule | Description |
|---|---|---|
| BroadcastSender | Every 5 min | Process queued broadcasts |
| OtpCleanup | Hourly | Delete expired OTPs |
| SubscriptionExpire | Daily | Check and update expired subscriptions |
| NotificationCleanup | Weekly | Clean old read notifications |

---

## 4. Definition of Done

- [ ] Scheduled job configuration
- [ ] Job monitoring endpoint
- [ ] Retry mechanism for failed jobs