package com.fleetmanagement.repository;

import com.fleetmanagement.entity.Vehicle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Vehicle Repository with optimized queries for scalability
 * Includes custom queries for vehicle and fleet management operations
 */
@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, UUID> {
    
    /**
     * Find vehicle by license plate
     */
    Optional<Vehicle> findByLicensePlate(String licensePlate);
    
    /**
     * Check if vehicle exists by license plate
     */
    boolean existsByLicensePlate(String licensePlate);
    
    /**
     * Find vehicles by status with pagination
     */
    Page<Vehicle> findByStatus(Vehicle.VehicleStatus status, Pageable pageable);
    
    /**
     * Find vehicles by tenant with pagination
     */
    Page<Vehicle> findByTenantId(UUID tenantId, Pageable pageable);
    
    /**
     * Find vehicles by status and tenant
     */
    Page<Vehicle> findByStatusAndTenantId(Vehicle.VehicleStatus status, UUID tenantId, Pageable pageable);
    
    /**
     * Find vehicles by fleet
     */
    Page<Vehicle> findByFleetId(UUID fleetId, Pageable pageable);
    
    /**
     * Find vehicles by brand (changed from make)
     */
    Page<Vehicle> findByBrand(String brand, Pageable pageable);
    
    /**
     * Find vehicles by brand and model
     */
    Page<Vehicle> findByBrandAndModel(String brand, String model, Pageable pageable);
    
    /**
     * Find vehicles by vehicle type
     */
    Page<Vehicle> findByVehicleType(Vehicle.VehicleType vehicleType, Pageable pageable);
    
    /**
     * Find vehicles by year range
     */
    @Query("SELECT v FROM Vehicle v WHERE v.year BETWEEN :startYear AND :endYear")
    Page<Vehicle> findByYearBetween(@Param("startYear") Integer startYear, @Param("endYear") Integer endYear, Pageable pageable);
    
    /**
     * Find vehicles assigned to specific user
     */
    @Query("SELECT v FROM Vehicle v JOIN v.users u WHERE u.id = :userId")
    Page<Vehicle> findByUserId(@Param("userId") UUID userId, Pageable pageable);
    
    /**
     * Find vehicles with device assigned
     */
    @Query("SELECT v FROM Vehicle v WHERE v.device IS NOT NULL")
    Page<Vehicle> findVehiclesWithDevice(Pageable pageable);
    
    /**
     * Find vehicles without device assigned
     */
    @Query("SELECT v FROM Vehicle v WHERE v.device IS NULL")
    Page<Vehicle> findVehiclesWithoutDevice(Pageable pageable);
    
    /**
     * Find vehicle by device ID
     */
    @Query("SELECT v FROM Vehicle v WHERE v.device.id = :deviceId")
    Optional<Vehicle> findByDeviceId(@Param("deviceId") UUID deviceId);
    
    /**
     * Count vehicles by status
     */
    long countByStatus(Vehicle.VehicleStatus status);
    
    /**
     * Count vehicles by tenant
     */
    long countByTenantId(UUID tenantId);
    
    /**
     * Count vehicles by fleet
     */
    long countByFleetId(UUID fleetId);
    
    /**
     * Find vehicles by VIN
     */
    Optional<Vehicle> findByVin(String vin);
    
    /**
     * Check if VIN exists
     */
    boolean existsByVin(String vin);
}