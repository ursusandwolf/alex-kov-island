export const getSpeciesColor = (code: string | null, isPlant: boolean): string => {
  // Empty cell color
  const isDark = document.documentElement.getAttribute('data-theme') === 'dark';
  if (!code) return isDark ? '#2d2d2d' : '#eeeeee';
  
  // Nature domain colors
  if (isPlant) return '#66bb6a'; // Vibrant green
  
  const normalized = code.toLowerCase();
  
  // Heuristic for animals
  // Predators: Red/Orange
  if (normalized === 'wolf' || normalized === 'bear' || normalized === 'fox') return '#ef5350'; 
  // Herbivores: Blue/Cyan
  if (normalized === 'rabbit' || normalized === 'deer' || normalized === 'caterpillar') return '#42a5f5'; 
  
  // SimCity domain colors
  const cityColors: Record<string, string> = {
    road: '#90a4ae',
    residential: '#81c784',
    commercial: '#64b5f6',
    industrial: '#ffd54f'
  };
  
  return cityColors[normalized] ?? '#ba68c8'; // Purple for others
};
