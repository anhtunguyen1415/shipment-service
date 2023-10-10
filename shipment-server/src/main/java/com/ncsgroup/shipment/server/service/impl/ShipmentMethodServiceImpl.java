package com.ncsgroup.shipment.server.service.impl;

import com.ncsgroup.shipment.server.dto.shipmentmethod.ShipmentMethodPageResponse;
import com.ncsgroup.shipment.server.dto.shipmentmethod.ShipmentMethodResponse;
import com.ncsgroup.shipment.server.entity.ShipmentMethod;
import com.ncsgroup.shipment.server.exception.shipmentmethod.ShipmentMethodAlreadyExistException;
import com.ncsgroup.shipment.server.exception.shipmentmethod.ShipmentMethodNotFoundException;
import com.ncsgroup.shipment.server.repository.ShipmentMethodRepository;
import com.ncsgroup.shipment.server.service.ShipmentMethodService;
import com.ncsgroup.shipment.server.service.base.BaseServiceImpl;
import dto.ShipmentMethodRequest;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class ShipmentMethodServiceImpl extends BaseServiceImpl<ShipmentMethod> implements ShipmentMethodService {
    private final ShipmentMethodRepository repository;

    public ShipmentMethodServiceImpl(ShipmentMethodRepository repository) {
        super(repository);
        this.repository = repository;
    }

    @Override
    @Transactional
    public ShipmentMethodResponse create(ShipmentMethodRequest request) {
        log.info("(create) request: {}", request);
        checkShipmentMethodAlreadyExists(request.getName());
        ShipmentMethod shipmentMethod = ShipmentMethod.from(
                request.getName(),
                request.getDescription(),
                request.getPricePerKilometer()
        );
        create(shipmentMethod);
        return convertShipmentMethodToShipmentMethodResponse(shipmentMethod);
    }

    @Override
    @Transactional
    public ShipmentMethodResponse update(String id, ShipmentMethodRequest request) {
        log.info("(update) request: {}", request);

        ShipmentMethod shipmentMethod = findById(id);
        checkNameOfShipmentMethodAlreadyExistsWhenUpdate(shipmentMethod, request);
        setValueForUpdate(shipmentMethod, request);
        shipmentMethod = update(shipmentMethod);
        return convertShipmentMethodToShipmentMethodResponse(shipmentMethod);
    }

    @Override
    public ShipmentMethodPageResponse list(String keyword, int size, int page, boolean isAll) {
        log.info("(list) keyword: {}, size : {}, page: {}, isAll: {}", keyword, size, page, isAll);
        List<ShipmentMethodResponse> list = new ArrayList<>();
        Pageable pageable = PageRequest.of(page, size);
        List<ShipmentMethod> shipmentMethods = isAll ?
                repository.findAll() : repository.search(keyword, pageable);
        for (ShipmentMethod shipmentMethod : shipmentMethods) {
            list.add(ShipmentMethodResponse.from(
                    shipmentMethod.getName(),
                    shipmentMethod.getDescription(),
                    shipmentMethod.getPricePerKilometer()
            ));
        }
        return new ShipmentMethodPageResponse(list, isAll ? shipmentMethods.size() : repository.countSearch(keyword));
    }

    public ShipmentMethod findById(String id) {
        log.info("(findById) id: {}", id);
        ShipmentMethod shipmentMethod = repository.findById(id).orElseThrow(ShipmentMethodNotFoundException::new);
        if (shipmentMethod.isDeleted())
            throw new ShipmentMethodNotFoundException();
        return shipmentMethod;
    }

    private void checkShipmentMethodAlreadyExists(String name) {
        log.info("checkShipmentMethodAlreadyExists :{}", name);
        if (repository.existsByName(name)) {
            log.error("Shipment Method AlreadyExists :{}, name");
            throw new ShipmentMethodAlreadyExistException();
        }
    }

    private void checkNameOfShipmentMethodAlreadyExistsWhenUpdate(ShipmentMethod shipmentMethod, ShipmentMethodRequest request) {
        log.info("check name of shipment method AlreadyExists when update");
        if (!shipmentMethod.getName().equals(request.getName()))
            checkShipmentMethodAlreadyExists(request.getName());
    }

    private void setValueForUpdate(ShipmentMethod shipmentmethod, ShipmentMethodRequest request) {
        shipmentmethod.setName(request.getName());
        shipmentmethod.setDescription(request.getDescription());
        shipmentmethod.setPricePerKilometer(request.getPricePerKilometer());
    }

    private ShipmentMethodResponse convertShipmentMethodToShipmentMethodResponse(ShipmentMethod shipmentMethod) {
        return ShipmentMethodResponse.from(
                shipmentMethod.getName(),
                shipmentMethod.getDescription(),
                shipmentMethod.getPricePerKilometer());
    }

}