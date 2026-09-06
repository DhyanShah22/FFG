// Data for Career Assessments
export const assessmentsData = [
  {
    id: "aptitude",
    title: "Aptitude",
    duration: "15 mins",
    questionsCount: 5,
    type: "aptitude",
    difficulty: "Medium",
    status: "pending", // pending, completed, locked
    timerEnabled: true,
    timerMinutes: 15,
    description: "Evaluates your analytical skills, spatial awareness, logical deduction, and verbal processing to find fields where you naturally excel.",
    accentClass: "primary-accent",
    iconName: "BrainCircuit",
    questions: [
      {
        id: "apt_1",
        question: "Select the figure that logically completes the pattern: If a wheel has 8 spokes, how many spaces are there between the spokes?",
        isMandatory: true,
        options: [
          { letter: "A", text: "7 spaces", weight: 0 },
          { letter: "B", text: "8 spaces", weight: 1 }, // Correct
          { letter: "C", text: "9 spaces", weight: 0 },
          { letter: "D", text: "16 spaces", weight: 0 }
        ],
        correctAnswer: "B",
        explanation: "Since the wheel is circular, the spokes radiate from the center to the edge. The number of spaces between spokes is equal to the number of spokes, which is 8."
      },
      {
        id: "apt_2",
        question: "A training batch has 60 students. The ratio of students interested in Tech to Healthcare is 3:2. Select ALL true statements below:",
        isMandatory: true,
        isMultiSelect: true,
        options: [
          { letter: "A", text: "36 students prefer Technology", weight: 1 }, // Correct
          { letter: "B", text: "24 students prefer Healthcare", weight: 1 }, // Correct
          { letter: "C", text: "40 students prefer Technology", weight: 0 },
          { letter: "D", text: "There are 12 more students in Tech than Healthcare", weight: 1 } // Correct: 36 - 24 = 12
        ],
        correctAnswer: "A",
        explanation: "Total ratio parts = 5 (12 students per part). Tech = 3*12 = 36. Healthcare = 2*12 = 24. Difference = 36 - 24 = 12."
      },
      {
        id: "apt_3",
        question: "An NGO is organizing career counseling session halls. Hall A is bigger than Hall B. Hall C is smaller than Hall B. Hall D is bigger than Hall C but smaller than Hall B. Which hall is the smallest?",
        isMandatory: false,
        options: [
          { letter: "A", text: "Hall A", weight: 0 },
          { letter: "B", text: "Hall B", weight: 0 },
          { letter: "C", text: "Hall C", weight: 1 }, // Correct: C is the smallest
          { letter: "D", text: "Hall D", weight: 0 }
        ],
        correctAnswer: "C",
        explanation: "From the description: A > B, B > C, and B > D > C. Arranging from largest to smallest gives: A > B > D > C. Thus, Hall C is the smallest."
      },
      {
        id: "apt_4",
        question: "Choose the word that means the opposite of 'APPRECIATE':",
        isMandatory: true,
        options: [
          { letter: "A", text: "Ignore", weight: 0 },
          { letter: "B", text: "Depreciate", weight: 1 }, // Correct word choice antonym
          { letter: "C", text: "Accept", weight: 0 },
          { letter: "D", text: "Praise", weight: 0 }
        ],
        correctAnswer: "B",
        explanation: "To appreciate means to increase in value or to recognize value. To depreciate means to diminish in value or speak slightingly of, making it the antonym."
      },
      {
        id: "apt_5",
        question: "Select ALL logical conclusions that follow: 'All programmers are problem solvers, and some problem solvers are social workers.'",
        isMandatory: false,
        isMultiSelect: true,
        options: [
          { letter: "A", text: "Some social workers are problem solvers", weight: 1 }, // Correct
          { letter: "B", text: "All programmers are social workers", weight: 0 },
          { letter: "C", text: "Problem solvers include both some social workers and all programmers", weight: 1 }, // Correct
          { letter: "D", text: "None of the programmers are social workers", weight: 0 }
        ],
        correctAnswer: "A",
        explanation: "If 'some problem solvers are social workers', then by conversion 'some social workers are problem solvers' is true. Also problem solvers contain all programmers by definition."
      }
    ]
  },
  {
    id: "personality",
    title: "Interests",
    duration: "10 mins",
    questionsCount: 6,
    type: "personality",
    difficulty: "Easy",
    status: "pending",
    timerEnabled: true,
    timerMinutes: 10,
    description: "Aligns your personal preferences, values, and energy sources with the Holland occupational codes to map your core interest profile.",
    accentClass: "accent-purple",
    iconName: "Compass",
    questions: [
      {
        id: "pers_1",
        question: "Which of the following activities sound exciting to spend a Saturday morning? (Select up to 2 that appeal to you)",
        isMandatory: true,
        isMultiSelect: true,
        options: [
          { letter: "A", text: "Assembling a mechanical kit or repairing a broken appliance (Realistic)", category: "R" },
          { letter: "B", text: "Reading a scientific paper or researching online about a mystery (Investigative)", category: "I" },
          { letter: "C", text: "Sketching, writing a poem, or composing a song (Artistic)", category: "A" },
          { letter: "D", text: "Volunteering to help children or teaching at a community center (Social)", category: "S" },
          { letter: "E", text: "Organizing a team of friends to sell entries for a charity raffle (Enterprising)", category: "E" },
          { letter: "F", text: "Updating spreadsheets or cataloging books in a database (Conventional)", category: "C" }
        ]
      },
      {
        id: "pers_2",
        question: "If you were working on a group project, what role would you naturally pick or feel most comfortable doing?",
        isMandatory: true,
        options: [
          { letter: "A", text: "Building the physical prototype/model using tools (Realistic)", category: "R" },
          { letter: "B", text: "Analyzing the data, running statistical simulations, or solving hard logic issues (Investigative)", category: "I" },
          { letter: "C", text: "Creating the visual slide deck, selecting branding colors, or designing layouts (Artistic)", category: "A" },
          { letter: "D", text: "Helping resolve group disagreements and checking in on everyone's feelings (Social)", category: "S" },
          { letter: "E", text: "Presenting the final project, pitching it to external judges, and leading discussion (Enterprising)", category: "E" },
          { letter: "F", text: "Compiling the final bibliography, formatting pages, and double-checking spelling/data (Conventional)", category: "C" }
        ]
      },
      {
        id: "pers_3",
        question: "Which workspace environments make you feel energized? (Select all that apply)",
        isMandatory: false,
        isMultiSelect: true,
        options: [
          { letter: "A", text: "An active workshop, garden, laboratory, or construction site (Realistic)", category: "R" },
          { letter: "B", text: "A quiet study room or computer terminal with access to lots of data charts (Investigative)", category: "I" },
          { letter: "C", text: "A messy creative studio, stage, drafting desk, or brainstorming lounge (Artistic)", category: "A" },
          { letter: "D", text: "A counseling clinic, workspace centered around team chat, or classroom (Social)", category: "S" },
          { letter: "E", text: "A corporate boardroom, sales office, or dynamic start-up incubator (Enterprising)", category: "E" },
          { letter: "F", text: "An elegant, well-ordered office room where databases are managed cleanly (Conventional)", category: "C" }
        ]
      },
      {
        id: "pers_4",
        question: "Which type of problems do you enjoy solving the most?",
        isMandatory: true,
        options: [
          { letter: "A", text: "Fixing mechanical failures, fixing plumbing, or setting up sound systems (Realistic)", category: "R" },
          { letter: "B", text: "Understanding why system bugs occur or discovering scientific principles (Investigative)", category: "I" },
          { letter: "C", text: "Expressing a complex feeling through a design, layout, or script (Artistic)", category: "A" },
          { letter: "D", text: "Helping a friend work through a major life struggle or study issue (Social)", category: "S" },
          { letter: "E", text: "Persuading someone to buy a product, invest in an idea, or vote for a policy (Enterprising)", category: "E" },
          { letter: "F", text: "Finding double-entries in bills or restoring chronological order to records (Conventional)", category: "C" }
        ]
      },
      {
        id: "pers_5",
        question: "Pick the book/video course that you would buy immediately:",
        isMandatory: false,
        options: [
          { letter: "A", text: "'A Hands-On Guide to DIY Home Projects and Electronics' (Realistic)", category: "R" },
          { letter: "B", text: "'Mind-Bending Riddles, Physics Mysteries, & Code Breaking' (Investigative)", category: "I" },
          { letter: "C", text: "'Storyboarding, Graphic Illustration, and Creative Expression' (Artistic)", category: "A" },
          { letter: "D", text: "'The Science of Mentorship, Counseling, and Team Empathy' (Social)", category: "S" },
          { letter: "E", text: "'Pitch Perfect: How to Speak with Influence and Sell Anything' (Enterprising)", category: "E" },
          { letter: "F", text: "'Excel Mastery: Advanced Data Organization & Financial Management' (Conventional)", category: "C" }
        ]
      },
      {
        id: "pers_6",
        question: "When people describe you, which phrase sounds closest to you?",
        isMandatory: true,
        options: [
          { letter: "A", text: "Practical, hands-on, down-to-earth person who likes physical activity (Realistic)", category: "R" },
          { letter: "B", text: "Curious, thoughtful, analytical thinker who likes to research facts (Investigative)", category: "I" },
          { letter: "C", text: "Imaginative, original, expressive person who values freedom (Artistic)", category: "A" },
          { letter: "D", text: "Friendly, helpful, supportive listener who loves to collaborative (Social)", category: "S" },
          { letter: "E", text: "Confident, ambitious, persuasive talker who likes to lead groups (Enterprising)", category: "E" },
          { letter: "F", text: "Organized, orderly, detailed, reliable manager of records (Conventional)", category: "C" }
        ]
      }
    ]
  },
  {
    id: "work_values",
    title: "Reality",
    duration: "10 mins",
    questionsCount: 5,
    type: "values",
    difficulty: "Easy",
    status: "completed", // pre-completed dummy data
    timerEnabled: true,
    timerMinutes: 10,
    description: "Identifies what satisfies you most in a job, whether it's independence, team collaboration, high status, or security.",
    accentClass: "secondary-accent",
    iconName: "Award",
    questions: []
  }
];

// Careers mapping database
export const careerDatabase = [
  {
    code: "SEC", // Social, Enterprising, Conventional
    careers: [
      { name: "Career Counselor & Youth Mentor", match: "98%", education: "BA/MA Psychology or Social Work", description: "Guides students on their education journey, advises them on career profiles, and leads group workshops." },
      { name: "Public Relations Specialist", match: "88%", education: "Degree in Communications / Media", description: "Coordinates publicity, writes articles, manages media relations to build brand brand image." },
      { name: "Human Resources Specialist", match: "85%", education: "BBA / MBA in Human Relations", description: "Handles recruiting, interviews candidates, guides orientation programs, and supports employee development." }
    ]
  },
  {
    code: "ISR", // Investigative, Social, Realistic
    careers: [
      { name: "Healthcare Professional (Nursing/Physiotherapy)", match: "95%", education: "B.Sc Nursing / BPTh", description: "Helps patients heal, provides compassionate bedside support, and monitors clinical progress." },
      { name: "Environmental Scientist", match: "90%", education: "B.Sc/M.Sc Environmental Science", description: "Researches ecological issues, tracks pollution, and advises community projects on sustainability." },
      { name: "Occupational Health & Safety Officer", match: "86%", education: "Diploma in Industrial Health & Safety", description: "Inspects work environments, reports hazards, and trains workforce on risk avoidance." }
    ]
  },
  {
    code: "IAC", // Investigative, Artistic, Conventional
    careers: [
      { name: "Database Administrator / Data Analyst", match: "94%", education: "B.Tech/BCA in Computer Science or Statistics", description: "Organizes corporate database systems, extracts strategic data charts, and builds analytics report dashboards." },
      { name: "Front-End Developer & UI Designer", match: "90%", education: "Bootcamp Certificate or B.Sc IT", description: "Bridges creative web layouts with logical code scripting. Writes code to build responsive components." },
      { name: "Technical Writer & Content Curator", match: "87%", education: "BA in Language, English, or Journalism", description: "Creates straightforward API user guides, documents systems logic, and organizes content guides." }
    ]
  },
  {
    code: "AES", // Artistic, Enterprising, Social
    careers: [
      { name: "Social Media Manager & Digital Designer", match: "96%", education: "Degree/Diploma in Design or Marketing", description: "Designs engaging layouts, writes compelling copy, and organizes organic marketing campaigns." },
      { name: "Community Coordinator & Event Manager", match: "92%", education: "Diploma in Event Management / Hospitality", description: "Brainstorms design concepts, manages sponsor accounts, and supervises smooth public operations." },
      { name: "UX Content Creator & Advocate", match: "89%", education: "Interdisciplinary training design & marketing", description: "Gathers student feedback and builds beautiful educational infographics to support NGO projects." }
    ]
  },
  {
    code: "RSE", // Realistic, Social, Enterprising
    careers: [
      { name: "Industrial Workshop Supervisor", match: "91%", education: "Vocational Diploma / Mechanical Training", description: "Guides junior mechanics, plans factory machine schedules, and manages equipment logistics." },
      { name: "Vocational Technical Coach", match: "89%", education: "ITI Certificate + Training coaching", description: "Instructs students on technical machines, evaluates safety practices, and connects youth to apprenticeships." }
    ]
  },
  {
    code: "CSI", // Conventional, Social, Investigative
    careers: [
      { name: "Medical Records Analyst", match: "92%", education: "Diploma in Medical Lab / Health Records", description: "Compiles patient health data, ensures strict compliance index, and prepares hospital status tables." },
      { name: "Education Program Coordinator", match: "88%", education: "Graduate in BA/B.Com + NGO experience", description: "Schedules center counseling times, records student marks, and organizes compliance spreadsheets." }
    ]
  }
];

// Fallback recommendations if user has a custom score
export const fallbackCareers = [
  { name: "Junior Software Developer", match: "85%", education: "B.Sc Computer Science / IT / BCA", description: "Develops, tests, and maintains applications. Solves logical bugs and writes structured algorithms." },
  { name: "Visual Graphic Illustrator", match: "82%", education: "Diploma in Graphic Design / Fine Arts", description: "Generates modern logo assets, custom icons, brand color guidelines, and media layouts." },
  { name: "NGO Community Mobilizer", match: "80%", education: "Any Graduate with communication skills", description: "Spreads awareness about student guidance workshops, coordinates with parents, and guides enrollments." }
];
