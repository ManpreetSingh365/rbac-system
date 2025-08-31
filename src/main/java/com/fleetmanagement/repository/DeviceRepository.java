package com.fleetmanagement.repository;

import com.fleetmanagement.entity.Device;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Device Repository with optimized queries for scalability
 * Includes custom queries for device management and SMS registration
 */
@Repository
public interface DeviceRepository extends JpaRepository<Device, UUID> {
    
    /**
     * Find device by IMEI
     */
    Optional<Device> findByImei(String imei);
    
    /**
     * Check if device exists by IMEI
     */
    boolean existsByImei(String imei);
    
    /**
     * Find devices by status with pagination
     */
    Page<Device> findByStatus(Device.DeviceStatus status, Pageable pageable);
    
    /**
     * Find devices by tenant with pagination
     */
    Page<Device> findByTenantId(UUID tenantId, Pageable pageable);
    
    /**
     * Find devices by status and tenant
     */
    Page<Device> findByStatusAndTenantId(Device.DeviceStatus status, UUID tenantId, Pageable pageable);
    
    /**
     * Find devices registered by SMS
     */
    Page<Device> findByRegisteredBySmsTrue(Pageable pageable);
    
    /**
     * Find devices by installer phone
     */
    Page<Device> findByInstallerPhone(String installerPhone, Pageable pageable);
    
    /**
     * Find devices by model
     */
    Page<Device> findByDeviceModel(String deviceModel, Pageable pageable);
    
    /**
     * Find devices with last heartbeat before specified time (inactive devices)
     */
    @Query("SELECT d FROM Device d WHERE d.lastHeartbeat < :beforeTime")
    Page<Device> findInactiveDevices(@Param("beforeTime") LocalDateTime beforeTime, Pageable pageable);
    
    /**
     * Find devices assigned to specific user
     */
    @Query("SELECT d FROM Device d JOIN d.users u WHERE u.id = :userId")
    Page<Device> findByUserId(@Param("userId") UUID userId, Pageable pageable);
    
    /**
     * Find device assigned to specific vehicle
     */
    @Query("SELECT d FROM Device d WHERE d.vehicle.id = :vehicleId")
    Optional<Device> findByVehicleId(@Param("vehicleId") UUID vehicleId);
    
    /**
     * Count devices by status
     */
    long countByStatus(Device.DeviceStatus status);
    
    /**
     * Count devices by tenant
     */
    long countByTenantId(UUID tenantId);
    
    /**
     * Find devices with expiry date before specified time
     */
    @Query("SELECT d FROM Device d WHERE d.expiryAt < :expiryTime")
    Page<Device> findExpiredDevices(@Param("expiryTime") LocalDateTime expiryTime, Pageable pageable);
    
    /**
     * Find devices expiring soon (within specified days)
     */
    @Query("SELECT d FROM Device d WHERE d.expiryAt BETWEEN :now AND :futureTime")
    Page<Device> findDevicesExpiringSoon(@Param("now") LocalDateTime now, @Param("futureTime") LocalDateTime futureTime, Pageable pageable);
}