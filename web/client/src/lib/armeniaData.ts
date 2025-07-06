export interface ArmeniaRegion {
  id: string;
  name: string;
  cities: string[];
  count: number;
  bounds: [[number, number], [number, number]];
  imageUrl: string;
  center: [number, number];
}

export interface CityCoordinates {
  [key: string]: [number, number];
}

export const armeniaRegions: ArmeniaRegion[] = [
  {
    id: 'yerevan',
    name: 'Yerevan',
    cities: ['Yerevan'],
    count: 1,
    bounds: [[40.15, 44.45], [40.22, 44.55]],
    imageUrl: '/assets/erevan_1751650662069.png',
    center: [40.1792, 44.4991]
  },
  {
    id: 'shirak',
    name: 'Shirak Province',
    cities: ['Gyumri', 'Artik', 'Maralik'],
    count: 3,
    bounds: [[40.65, 43.6], [41.1, 44.2]],
    imageUrl: '/assets/shirak_1751650662070.png',
    center: [40.85, 43.9]
  },
  {
    id: 'lori',
    name: 'Lori Province',
    cities: ['Vanadzor', 'Alaverdi', 'Stepanavan', 'Spitak', 'Tumanyan'],
    count: 5,
    bounds: [[40.65, 44.15], [41.45, 45.1]],
    imageUrl: '/assets/Lori_1751650662070.png',
    center: [41.05, 44.6]
  },
  {
    id: 'kotayk',
    name: 'Kotayk Province',
    cities: ['Hrazdan', 'Charentsavan', 'Abovyan', 'Nor Hachn'],
    count: 4,
    bounds: [[40.0, 44.15], [40.75, 44.85]],
    imageUrl: '/assets/Kotayq_1751650662070.png',
    center: [40.35, 44.5]
  },
  {
    id: 'gegharkunik',
    name: 'Gegharkunik Province',
    cities: ['Gavar', 'Sevan', 'Vardenis', 'Martuni'],
    count: 4,
    bounds: [[40.0, 44.75], [40.85, 46.5]],
    imageUrl: '/assets/gegharquniq_1751650662070.png',
    center: [40.4, 45.6]
  },
  {
    id: 'tavush',
    name: 'Tavush Province',
    cities: ['Ijevan', 'Berd', 'Dilijan', 'Noyemberyan'],
    count: 4,
    bounds: [[40.75, 44.75], [41.35, 45.7]],
    imageUrl: '/assets/Tavush_1751650662071.png',
    center: [41.05, 45.2]
  },
  {
    id: 'ararat',
    name: 'Ararat Province',
    cities: ['Artashat', 'Ararat', 'Masis', 'Echmiadzin'],
    count: 4,
    bounds: [[39.65, 43.95], [40.25, 44.85]],
    imageUrl: '/assets/ararat_1751650662069.png',
    center: [39.95, 44.4]
  },
  {
    id: 'armavir',
    name: 'Armavir Province',
    cities: ['Armavir', 'Vagharshapat', 'Metsamor'],
    count: 3,
    bounds: [[39.85, 43.75], [40.35, 44.45]],
    imageUrl: '/assets/Armavir_1751650662069.png',
    center: [40.1, 44.1]
  },
  {
    id: 'vayots_dzor',
    name: 'Vayots Dzor Province',
    cities: ['Yeghegnadzor', 'Jermuk', 'Areni', 'Vayk'],
    count: 4,
    bounds: [[39.35, 44.75], [40.05, 46.5]],
    imageUrl: '/assets/VayotsDzor_1751650662071.png',
    center: [39.7, 45.6]
  },
  {
    id: 'syunik',
    name: 'Syunik Province',
    cities: ['Kapan', 'Goris', 'Sisian', 'Meghri', 'Kajaran'],
    count: 5,
    bounds: [[38.75, 45.1], [39.75, 46.7]],
    imageUrl: '/assets/syunik_1751650662071.png',
    center: [39.25, 45.9]
  },
  {
    id: 'aragatsotn',
    name: 'Aragatsotn Province',
    cities: ['Ashtarak', 'Aparan', 'Talin', 'Byureghavan'],
    count: 4,
    bounds: [[40.05, 43.7], [40.75, 44.5]],
    imageUrl: '/assets/aragatsotn_1751650662069.png',
    center: [40.4, 44.1]
  }
];

export const citiesCoordinates: CityCoordinates = {
  // Yerevan
  'Yerevan': [40.1792, 44.4991],
  
  // Shirak Province
  'Gyumri': [40.7894, 43.8419],
  'Artik': [40.5167, 43.8833],
  'Maralik': [40.5667, 43.8667],
  
  // Lori Province
  'Vanadzor': [40.8058, 44.4818],
  'Alaverdi': [41.1000, 44.6500],
  'Stepanavan': [41.0167, 44.3667],
  'Spitak': [40.8333, 44.2667],
  'Tumanyan': [41.0833, 44.6333],
  
  // Kotayk Province
  'Hrazdan': [40.5000, 44.7667],
  'Charentsavan': [40.3167, 44.7333],
  'Abovyan': [40.2667, 44.6333],
  'Nor Hachn': [40.1833, 44.6833],
  
  // Gegharkunik Province
  'Gavar': [40.3500, 45.1333],
  'Sevan': [40.5500, 44.9500],
  'Vardenis': [40.1833, 45.7333],
  'Martuni': [40.1333, 45.3167],
  
  // Tavush Province
  'Ijevan': [40.8833, 45.1500],
  'Berd': [40.8833, 45.3833],
  'Dilijan': [40.7333, 44.8667],
  'Noyemberyan': [41.2000, 45.0333],
  
  // Ararat Province
  'Artashat': [39.9667, 44.5500],
  'Ararat': [39.8667, 44.7167],
  'Masis': [40.0833, 44.4000],
  'Echmiadzin': [40.1616, 44.2910],
  
  // Vayots Dzor Province
  'Yeghegnadzor': [39.7667, 45.3500],
  'Jermuk': [39.8333, 45.6667],
  'Areni': [39.7167, 45.1833],
  'Vayk': [39.6833, 45.4667],
  
  // Syunik Province
  'Kapan': [39.2000, 46.4000],
  'Goris': [39.5167, 46.3333],
  'Sisian': [39.5167, 46.0333],
  'Meghri': [38.9000, 46.2333],
  'Kajaran': [39.1500, 46.1167],
  
  // Aragatsotn Province
  'Ashtarak': [40.3000, 44.3667],
  'Aparan': [40.5833, 44.3500],
  'Talin': [40.3833, 43.8667],
  'Byureghavan': [40.3000, 44.5167],
  
  // Additional notable locations
  'Garni': [40.1128, 44.7308],
  'Geghard': [40.1500, 44.8167],
  'Tatev': [39.3833, 46.2833],
  'Khor Virap': [39.8833, 44.5833]
};

export const armeniaCenter: [number, number] = [40.1792, 44.4991];
export const armeniaBounds: [[number, number], [number, number]] = [[38.8, 43.4], [41.4, 46.6]];
