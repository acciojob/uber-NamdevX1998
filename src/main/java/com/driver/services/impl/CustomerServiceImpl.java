package com.driver.services.impl;

import com.driver.model.*;
import com.driver.repository.CabRepository;
import com.driver.services.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.driver.repository.CustomerRepository;
import com.driver.repository.DriverRepository;
import com.driver.repository.TripBookingRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CustomerServiceImpl implements CustomerService {

	@Autowired
	CustomerRepository customerRepository2;

	@Autowired
	DriverRepository driverRepository2;

	@Autowired
	TripBookingRepository tripBookingRepository2;

	@Autowired
	CabRepository cabRepository;

	@Override
	public void register(Customer customer) {
		//Save the customer in database
		customerRepository2.save(customer);
	}

	@Override
	public void deleteCustomer(Integer customerId) {
		// Delete customer without using deleteById function
		Optional<Customer>optionalCustomer=customerRepository2.findById(customerId);
		if(optionalCustomer.isPresent()){
			Customer customer=optionalCustomer.get();
			customerRepository2.delete(customer);
		}
	}

	@Override
	public TripBooking bookTrip(int customerId, String fromLocation, String toLocation, int distanceInKm) throws Exception{
		//Book the driver with lowest driverId who is free (cab available variable is Boolean.TRUE). If no driver is available,
		//throw "No cab available!" exception
		//Avoid using SQL query
		Optional<Customer>optionalCustomer=customerRepository2.findById(customerId);
		if(!optionalCustomer.isPresent()) {
			throw new Exception("customer id is invalid");
		}
		Customer customer = optionalCustomer.get();

		List<Driver>driverList=driverRepository2.findAll();
		boolean flag=false;
		TripBooking tripBooking=new TripBooking();
		for(Driver driver:driverList){
			Cab cab=driver.getCab();
			boolean temp=cab.isAvailable();
			if(temp){
				flag=true;
				tripBooking.setFromLocation(fromLocation);
				tripBooking.setToLocation(toLocation);
				tripBooking.setDistanceInKm(distanceInKm);
				tripBooking.setTripStatus(TripStatus.CONFIRMED);
				tripBooking.setCustomer(customer);
				tripBooking.setDriver(driver);
				int perKmRate=cab.getPerKmRate();
				cab.setAvailable(false);
				cab.setDriver(driver);
				tripBooking.setBill(perKmRate*distanceInKm);
				driver.setCab(cab);
				customer.getTripBooking().add(tripBooking);
				driver.getTripBooking().add(tripBooking);
				driverRepository2.save(driver);
			}
		}
		if(flag==false){
			throw new Exception("No cab available!");
		}
		tripBookingRepository2.save(tripBooking);
		return tripBooking;
	}

	@Override
	public void cancelTrip(Integer tripId){
		//Cancel the trip having given trip Id and update TripBooking attributes accordingly
		TripBooking tripBooking=tripBookingRepository2.findById(tripId).get();
		if(tripBooking==null)
			return;
		tripBooking.setTripStatus(TripStatus.CANCELED);
		tripBooking.setBill(0);
		tripBooking.getDriver().getCab().setAvailable(Boolean.TRUE);
		tripBookingRepository2.save(tripBooking);
	}

	@Override
	public void completeTrip(Integer tripId){
		//Complete the trip having given trip Id and update TripBooking attributes accordingly.
		TripBooking tripBooking=tripBookingRepository2.findById(tripId).get();
		if(tripBooking==null)
			return;
		tripBooking.setTripStatus(TripStatus.COMPLETED);
		int bill=tripBooking.getDriver().getCab().getPerKmRate()*(tripBooking.getDistanceInKm());
		tripBooking.setBill(bill);
		tripBooking.getDriver().getCab().setAvailable(Boolean.TRUE);
		tripBookingRepository2.save(tripBooking);
	}
}
