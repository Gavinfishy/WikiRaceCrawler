# WikiRace Path Finder

## Overview
This project is a **WikiRace Path Finder** that finds the shortest path between two Wikipedia pages using a bidirectional search strategy. It leverages **JSoup** to crawl Wikipedia and traverse links starting from both the source and target pages until a connection is found.

### Play the game online:
[WikiRace Game](https://wiki-race.com/)

## How It Works
- Uses **JSoup** to scrape Wikipedia pages.
- Implements a **breadth-first search (BFS)** algorithm from both the start and end points.
- Detects disallowed pages using `robots.txt`.
- Constructs and returns the shortest path of Wikipedia links from the source to the target.

## Technologies Used
- **Java**
- **JSoup (for web scraping)**
- **BFS traversal algorithm**

## Code Structure
```
📂 WikiRacePathFinder
 ├── 📄 Main.java        # Entry point of the program
 ├── 📄 WikiCrawler.java # Crawler for finding paths between Wiki pages
 ├── 📄 PageNode.java    # Node representation of a Wikipedia page
 ├── 📄 README.md        # Project documentation
```

## Usage
### Setup
Ensure you have **Java 8+** installed.

### Run the Program
```sh
javac Main.java
java Main
```

### Example Output
```sh
Tennis -> Sports -> Video game -> Fortnite
```
This means the program found a path from the **Tennis** Wikipedia page to **Fortnite** via **Sports** and **Video game**.


## Future Enhancements
- Optimize search with **parallel processing**.
- Implement a **web UI** for visualization.
- Add **heuristic-based search algorithms** for faster results.

## Contributors
- [Connor Tynan](https://github.com/connortynan)
- [Gavin Fisher](https://github.com/Gavinfishy)
