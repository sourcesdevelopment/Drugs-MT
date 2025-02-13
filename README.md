# Drugs-MT
This is a plugin made by zitronekuchen that i updated and the owner of the server this was for scammed me (i was hosting the server for them so i am leaving this here)

# InfinityFarming Plugin
InfinityFarming is a Minecraft 1.12.2 plugin that introduces a unique farming system where players can grow, harvest, and process two types of drugs—Weed and Coke—using custom NPC interactions and timed growth mechanics. The plugin integrates with popular tools such as WorldGuard (for region/plot protection) and Citizens (for NPC functionality) to create an immersive experience.

Features
Drug Farming:

Weed: Plant and grow weed using special weed seeds. Harvested weed is represented by a Poisonous Potato with a custom name.
Coke: Plant and grow coke using coke seeds on Podzol blocks (with data value 2). Harvested coke is represented by Sugar with a custom name.
NPC Interactions:

Weed Seller NPC: Right-click a Citizens NPC with the Dealer trait to sell your weed.
Coke Seller NPC: Right-click a Citizens NPC with the Professor trait to sell your coke.
Coke Converter NPC: Right-click a Citizens NPC with the CokeConverter trait to convert coke leaves (DOUBLE_PLANT with specific data and custom name) into coke items. A conversion fee applies per leaf.
Realistic Farming Mechanics:

Timed planting processes where players cannot move until planting is complete.
Crop growth managed by internal timers and stored persistently in a SQLite database.
Economy Integration:

Uses Vault to deposit earnings when players sell their crops or pay conversion fees.
Protected Regions & Plots:

Leverages WorldGuard to ensure that weed can only be planted on your own plot and that coke is grown in the designated coke farming region.
Dependencies
Minecraft Version: 1.12.2
WorldGuard: Required for region and plot protection.
Citizens: Required for NPC functionality and custom traits.
Vault: Provides economy integration.
SQLite: Used for persistent storage of plant data.
Installation
Download the Plugin:
Place the InfinityFarming.jar file into your server's plugins folder.

Install Dependencies:
Make sure that WorldGuard, Citizens, and Vault are installed and running on your server.

Configure the Plugin:

Start your server so that the plugin generates its default config.yml.
Open the config.yml file to customize settings such as item names, prices, growth times, and crop readiness states.
Example configuration options:
WietName: The display name for weed items.
CokeName: The display name for coke items.
CokeLeaveName: The display name for coke leaves.
WeedPrice / CokePrice: Selling prices for weed and coke.
CokeConversionPrice: Fee per leaf when converting coke leaves into coke.
PlantingTime: Duration of the planting process.
WeedGrowing / CokeGrowing: Growth delays for the respective crops.
WeedState / CokeState: The state at which the crop is ready for harvest.
Restart the Server:
After configuring, restart your server to enable the plugin.

Usage
Planting
Weed Planting:

Use the Weed Seed (item name matching WietSeedName in the config) while standing on your own plot (verified by WorldGuard).
Upon planting, a timer begins during which your movement is restricted. When complete, a weed crop (block type CROPS) is planted and registered by the plugin.
Coke Planting:

Use the Coke Seed (item name matching CokeSeedName in the config) on a Podzol block (specifically, a dirt block with data value 2) within a designated Coke region.
Similar to weed planting, a timer starts to simulate the planting process. When complete, the block is updated (to LONG_GRASS with the correct data value) and a new coke crop is registered.
Harvesting and Growth
Crops gradually grow through multiple states based on the configured growth timers.
Once the growth state reaches the defined readiness level (WeedState for weed and CokeState for coke), the crop is ready for harvesting.
NPC Interactions
Selling Your Crops:

Weed Seller: Right-click an NPC with the Dealer trait to sell all matching weed items from your inventory.
Coke Seller: Right-click an NPC with the Professor trait to sell all matching coke items from your inventory.
In both cases, the plugin calculates the total price based on the number of items and deposits money into your account via Vault.
Converting Coke Leaves:

Right-click an NPC with the CokeConverter trait to convert coke leaves (DOUBLE_PLANT items with the name matching CokeLeaveName) into coke items.
The NPC will check your inventory, remove the leaves, and, if you have sufficient funds (if a conversion price is set), convert them into coke—splitting into stacks as necessary.
Database
The plugin uses an SQLite database to store plant data persistently.
Plants (both weed and coke) are saved with their world, coordinates, and growth state. Duplicate plants are ignored during the loading process.
Troubleshooting
NPC Not Responding:
Ensure that Citizens is correctly installed and that the NPCs have the appropriate traits (Dealer, Professor, or CokeConverter).

Region/Plot Issues:
Verify that WorldGuard is active and that the required regions and plots are properly set up (for example, ensuring that you are on your own plot when planting weed).

Economy Issues:
Make sure Vault is installed and linked to a compatible economy plugin.

Plant Growth/Database Problems:
Check your server console for any SQLite-related errors. Ensure your server environment supports SQLite and that the plugin has the necessary permissions to create and write to its database file.

