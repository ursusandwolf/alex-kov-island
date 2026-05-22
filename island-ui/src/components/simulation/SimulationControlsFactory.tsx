import React from 'react';
import { SimulationType } from '../../controller/SimulationType'; // Assuming types from backend are available or redefined here
import { NatureControls } from './NatureControls';
import { SimCityControls } from './SimCityControls';

interface FactoryProps {
  type: 'nature' | 'simcity';
  onStart: (type: string, width: number, height: number, tickMs: number) => void;
}

export const SimulationControlsFactory = ({ type, onStart }: FactoryProps) => {
  switch (type) {
    case 'nature':
      return <NatureControls onStart={onStart} />;
    case 'simcity':
      return <SimCityControls onStart={onStart} />;
    default:
      return null;
  }
};
