export interface Node {
	title: string;
	url: string;
	// This should be `Component` after lucide-svelte updates types
	// eslint-disable-next-line @typescript-eslint/no-explicit-any
	icon?: any;
	items?: Node[];
}

class NavNode implements Node {
	title: string;
	url: string;
	icon?: any;
	items?: Node[] | undefined;

	public constructor(title: string, url: string) {
		this.title = title;
		this.url = url;
		this.items = [];
	}

	public setIcon(icon: any): NavNode {
		this.icon = icon;
		return this;
	}

	public add(
		title: string,
		url: string,
		callback: ((node: NavNode) => void) | null = null
	): NavNode {
		let node: NavNode = new NavNode(title, url);
		if (callback) {
			callback(node);
		}
		this.items?.push(node);
		return this;
	}

	public addIf(
		condition: boolean | undefined,
		title: string,
		url: string,
		callback: ((node: NavNode) => void) | null = null
	): NavNode {
		if (condition) {
			this.add(title, url, callback);
		}
		return this;
	}
}

export class NavBuilder {
	private nodes: NavNode[];
	constructor() {
		this.nodes = [];
	}

	public add(
		title: string,
		url: string,
		callback: ((node: NavNode) => void) | null = null
	): NavBuilder {
		let node: NavNode = new NavNode(title, url);
		if (callback) {
			callback(node);
		}
		this.nodes.push(node);
		return this;
	}

	public addIf(
		condition: boolean | undefined,
		title: string,
		url: string,
		callback: ((node: NavNode) => void) | null = null
	): NavBuilder {
		if (condition) {
			this.add(title, url, callback);
		}
		return this;
	}

	public get(): NavNode[] {
		return this.nodes;
	}
}
