from typing import List
from .devices.base import IoTDeviceSimulator
from .devices.bin_sensor import BinSimulator
from .devices.soil_sensor import SoilSimulator
from .devices.light_sensor import LightSimulator

def build_park_fleet(park_id: int = 1) -> List[IoTDeviceSimulator]:
    """Instantiate the 15 baseline park sensors corresponding to DB seed data."""
    fleet: List[IoTDeviceSimulator] = [
        # Zone 1: Central Plaza & Promenade
        BinSimulator(1, "DEV-BIN-0001", "Plaza North Solar Bin", zone_id=1, park_id=park_id, initial_fill=65.0, initial_battery=94.5),
        BinSimulator(2, "DEV-BIN-0002", "Plaza Fountain South Bin", zone_id=1, park_id=park_id, initial_fill=42.0, initial_battery=88.0),
        SoilSimulator(7, "DEV-SOIL-0001", "Central Lawn Soil Monitor", zone_id=1, park_id=park_id, initial_moisture=38.5, initial_battery=96.0),
        LightSimulator(12, "DEV-LIGHT-0001", "Plaza Entrance Solar Luminaire", zone_id=1, park_id=park_id, initial_battery=98.0),

        # Zone 2: North Botanical Gardens
        BinSimulator(3, "DEV-BIN-0003", "Botanical Rose Garden Bin", zone_id=2, park_id=park_id, initial_fill=55.0, initial_battery=91.2),
        BinSimulator(4, "DEV-BIN-0004", "Botanical Greenhouse Bin", zone_id=2, park_id=park_id, initial_fill=78.5, initial_battery=79.5),
        SoilSimulator(8, "DEV-SOIL-0002", "Botanical Orchid Bed Probe", zone_id=2, park_id=park_id, initial_moisture=52.0, initial_battery=92.5),
        SoilSimulator(9, "DEV-SOIL-0003", "Botanical Arboretum Probe", zone_id=2, park_id=park_id, initial_moisture=46.0, initial_battery=89.0),
        LightSimulator(13, "DEV-LIGHT-0002", "Botanical Pathway LED 01", zone_id=2, park_id=park_id, initial_battery=95.0),

        # Zone 3: West Sports & Recreation Complex
        BinSimulator(5, "DEV-BIN-0005", "Sports Bleachers Bin", zone_id=3, park_id=park_id, initial_fill=81.0, initial_battery=82.0),
        BinSimulator(6, "DEV-BIN-0006", "Sports Pavilion Bin", zone_id=3, park_id=park_id, initial_fill=34.0, initial_battery=65.4),
        SoilSimulator(10, "DEV-SOIL-0004", "Football Pitch East Probe", zone_id=3, park_id=park_id, initial_moisture=31.0, initial_battery=78.0),
        SoilSimulator(11, "DEV-SOIL-0005", "Football Pitch West Probe", zone_id=3, park_id=park_id, initial_moisture=29.5, initial_battery=84.5),
        LightSimulator(14, "DEV-LIGHT-0003", "Jogging Track LED Pole A", zone_id=3, park_id=park_id, initial_battery=89.0),
        LightSimulator(15, "DEV-LIGHT-0004", "Jogging Track LED Pole B", zone_id=3, park_id=park_id, initial_battery=91.5),
    ]
    return fleet
