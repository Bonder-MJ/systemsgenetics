library(tidygraph)
library(ggraph)
library(data.table)

#path           <- "/home/work/Desktop/depict2/coregulation_data/HP:0000991"
#phenotype      <- "30690_raw"

#path           <- "/home/work/Desktop/depict2/coregulation_data/HP:0003124/"
#phenotype      <- "30690_raw"

path           <- "/home/work/Desktop/depict2/coregulation_data/HP:0011153/"
phenotype      <- "educational_attainment_2018_30038396_hg19"


path           <- "/home/work/Desktop/depict2/coregulation_data/alzheimers_top10/"
phenotype      <- "alzheimers_disease_2018_29777097_hg19"

path           <- "/home/work/Desktop/depict2/coregulation_data/HP:0001519//"
phenotype      <- "height_2018_30124842_hg19"

path           <- "/home/work/Desktop/depict2/coregulation_data/ced_top10coreg//"
phenotype      <- "coeliac_disease_2010_20190752_hg19"

ngwas          <- 100
nhpo           <- 20
edge.threshold <- 2

ensembl <- read.table("~/Documents/data/reference/ensembl/ensembl_gene_position_export.txt", sep="\t", header=T, stringsAsFactors = F)
ensembl <- unique(ensembl[,c(1, 5)])
rownames(ensembl) <- ensembl[,1]

read.coreg.data <- function(path, phenotype, ngene=100, nhpo=10, add.edges.for.hpo=F) {
  
  out <- list()
  genep.tmp       <- fread(paste0(path, "/", phenotype, "_gene_pvalues.txt"), data.table = F)
  genep           <- genep.tmp[,2]
  names(genep)    <- genep.tmp[,1]
  genep[is.na(genep)] <- 1
  genep           <- genep[order(genep)]
  
  tmp <- sum(genep == min(genep, na.rm=T), na.rm=T)
  if (tmp >= ngene) {
    print(paste0("[WARN] Detected ", tmp, " ties"))
    ngene <- tmp
  }

  genes.to.keep   <- names(genep[1:ngene])
  
  coreg.tmp       <- fread(paste0(path, "/", phenotype, "_coregulation.txt"), data.table = F)
  crg             <- coreg.tmp[,-1]
  names(crg)      <- coreg.tmp[,1]
  crg             <- crg[order(crg, decreasing=T)][1:nhpo]
  
  coreg           <- fread(paste0(path, "/", phenotype, "_coreg_hpo_genes.txt"), data.table = F)
  rownames(coreg) <- coreg[,1]
  
  if (add.edges.for.hpo) {
    genes.to.keep <- c(genes.to.keep, intersect(names(crg), rownames(coreg)))
  }
  
  coreg.out       <- coreg[unique(genes.to.keep),-1][,names(crg)]
  
  # Ugly but works, too lazy to do properly atm
  coreg.tmp       <- fread(paste0(path, "/", phenotype, "_coregulation.txt"), data.table = F)
  crg             <- coreg.tmp[,-1]
  names(crg)      <- coreg.tmp[,1]
  
  out$gene.pvalues <- genep
  out$coregulation <- crg
  out$data         <- coreg.out
  out$full.matrix  <- coreg
  
  return(out)
}

depict.to.network <- function(cur.data, edge.threshold=2, ngwas=100) {
  out             <- list()
  cur.df          <- cur.data$data
  nodes           <- as.data.frame(c(colnames(cur.df), rownames(cur.df)), stringsAsFactors = F)
  nodes$annot     <- rep("", nrow(nodes))
  nodes$annot[nodes[,1] %in% names(cur.data$gene.pvalues)[1:ngwas]] <- "GWAS gene"
  nodes$annot[nodes[,1] %in% colnames(cur.df)] <- "Mendelian gene" 
  nodes$annot[nodes$annot == "Mendelian gene" & nodes[,1] %in% names(cur.data$gene.pvalues)[1:ngwas] ] <- "Both"
  
  colnames(nodes) <- c("gene_id", "annot")
  nodes$annot     <- as.character(nodes$annot)
  #nodes$gene_name <- as.character(c(ensembl[colnames(cur.df), 2], rep(NA, nrow(cur.df))))
  nodes$gene_name <- as.character(ensembl[nodes$gene_id, 2])
  nodes$zscore    <- cur.data$coregulation[nodes$gene_id]
  #nodes <- na.omit(nodes)
  
  # Set negative coregulation to zero
  nodes[nodes$zscore < 0, ]$zscore <- 0
  
  # Remove duplicated nodes from node list, keeping the first (mendelian gene)
  nodes           <- nodes[!duplicated(nodes$gene_id),]
  rownames(nodes) <- nodes$gene_id
  #nodes[intersect(colnames(cur.df), rownames(nodes[nodes$annot=='GWAS gene',])), "annot"] <- "Both"
  nodes           <- nodes[order(nodes$annot, decreasing = T),]
  
  # Construct edges at zscore threshold
  tmp.edges       <- cur.df >= edge.threshold
  edges           <- as.data.frame(matrix(nrow=1, ncol=3))
  colnames(edges) <- c("from", "to", "zscore")
  
  # Remove duplicated edges for nodes which appear in both columns and rows
  ol <- intersect(rownames(cur.df), colnames(cur.df))
  tmp.edges[ol, ol][upper.tri(tmp.edges[ol, ol])] <- F
  
  for (row in 1:nrow(tmp.edges)) {
    for(col in 1:ncol(tmp.edges)) {
      if (tmp.edges[row, col]) {
          #edges <- rbind(edges, c((ncol(tmp.edges) + row), col, cur.df[row, col]))
          rowgene <- rownames(cur.df)[row]
          colgene <- colnames(cur.df)[col]
          edges   <- rbind(edges, c(
            which(nodes$gene_id == rowgene),
            which(nodes$gene_id == colgene),
            cur.df[row, col]))
      }
    }
  }
  
  edges <- na.omit(edges)
  #nodes <- nodes[unique(c(edges[,1], edges[,2])), ,drop=F]
  
  #top.zscore.genes <- unique(nodes[edges[order(edges$zscore, decreasing = T),1][1:10], "gene_id"])
  #nodes[top.zscore.genes, "gene_name"] <- ensembl[top.zscore.genes, 2]
  
  edge.stats <- t(sapply(nodes$gene_id, function(id){
    cur.edge.zscores <- edges[rownames(nodes)[edges$from] %in% id | rownames(nodes)[edges$to] %in% id, "zscore"]
    if (length(cur.edge.zscores) >= 1) {
      return(c(max(cur.edge.zscores, na.rm=T), length(cur.edge.zscores)))
    } else {
      return(c(0, 0))
    }
    }))
  nodes$max_edge_zscore <- edge.stats[,1]
  nodes$n_edges <- edge.stats[,2]
  
  tbl   <- tbl_graph(nodes=nodes, edges=edges, directed=F)
  return(list(graph=tbl, nodes=nodes, edges=edges))
}

cur.data <- read.coreg.data(path, phenotype, ngene=ngwas, nhpo=nhpo, add.edges.for.hpo = T)
network  <- depict.to.network(cur.data, edge.threshold=edge.threshold, ngwas=ngwas)

#Make plots
pdf(width=15, height=12, useDingbats = F, file=paste0("~/Desktop/depict2/", phenotype, "_edges_", edge.threshold, "_network_plot.pdf"))
par(xpd=NA)
p <- ggraph(network$graph,layout="linear", circular=T) +
  geom_edge_arc(aes(width = zscore, alpha=zscore))+
  geom_node_point(aes(colour=annot, size=zscore)) +
  theme_graph(base_family = 'sans') +
  scale_size(range=c(1,5), name="Depict2 (zscore)") +
  scale_edge_width(range = c(1, 4), name="Coregulation (Zscore)") +
  scale_edge_alpha(range =c(0.3, 0.75), name="Coregulation (Zscore)") +
  scale_color_manual(name="Gene type", values=c(`Both`="#FFBF40",
                                                `GWAS gene`="#33CCCC",
                                                `Mendelian gene`="#FF4040")) +
  guides(colour = guide_legend(override.aes = list(size=5)))

p
p + geom_node_label(aes(label=gene_name, fill=annot),
                    colour="white",
                    show.legend = F,
                    label.size = 0,
                    repel=T, segment.colour="black") +
  scale_fill_manual(values=c(`Both`="#FFBF40",
                             `GWAS gene`="#33CCCC",
                             `Mendelian gene`="#FF4040"))

# Remove nodes without an edge
sub_mygraph <- to_subgraph(network$graph, gene_id %in% network$nodes[unique(c(network$edges[,1], network$edges[,2])), 1], subset_by = "nodes")$subgraph

p2 <- ggraph(sub_mygraph, layout="linear", circular=T)  +
  geom_edge_arc(aes(width = zscore, alpha=zscore)) +
  geom_node_point(aes(colour=annot, size=zscore)) +
  theme_graph(base_family = 'sans') +
  scale_size(range=c(1,5), name="Depict2 (zscore)") +
  scale_edge_width(range = c(1, 4), name="Coregulation (Zscore)") +
  scale_edge_alpha(range =c(0.3, 0.75), name="Coregulation (Zscore)") +
  scale_color_manual(name="Gene type", values=c(`Both`="#FFBF40",
                                                  `GWAS gene`="#33CCCC",
                                                  `Mendelian gene`="#FF4040")) +
  guides(colour = guide_legend(override.aes = list(size=5)))

p2
p2 + geom_node_label(aes(label=gene_name, fill=annot),
                     colour="white",
                     show.legend = F,
                     label.size = 0,
                     repel=T, segment.colour="black") +
  scale_fill_manual(values=c(`Both`="#FFBF40",
                             `GWAS gene`="#33CCCC",
                             `Mendelian gene`="#FF4040"))


p3 <- ggraph(sub_mygraph, layout="gem") +
  geom_edge_link(aes(width = zscore, alpha=zscore)) +
  geom_node_point(aes(colour=annot, size=zscore)) +
  theme_graph(base_family = 'sans') +
  scale_size(range=c(1,5), name="Depict2 (zscore)") +
  scale_edge_width(range = c(1, 4), name="Coregulation (Zscore)") +
  scale_edge_alpha(range =c(0.3, 0.75), name="Coregulation (Zscore)") +
  scale_color_manual(name="Gene type", values=c(`Both`="#FFBF40",
                                                  `GWAS gene`="#33CCCC",
                                                  `Mendelian gene`="#FF4040")) +
  guides(colour = guide_legend(override.aes = list(size=5)))


p3
p3 + geom_node_label(aes(label=gene_name, fill=annot),
                     colour="white",
                     show.legend = F,
                     label.size = 0,
                     repel=T, segment.colour="black") +
  scale_fill_manual(values=c(`Both`="#FFBF40",
                             `GWAS gene`="#33CCCC",
                             `Mendelian gene`="#FF4040"))


dev.off()


# For patrick
write.table(network$nodes,file="~/Desktop/depict2/height_nodes_tall_stature_for_patrick.tsv", sep="\t", row.names=F, quote=F)
tmp <- cbind(rownames(network$nodes)[network$edges$from], rownames(network$nodes)[network$edges$to], network$edges$zscore)
colnames(tmp) <- c("from", "to", "zscore")
write.table(tmp, file="~/Desktop/depict2/height_edges_tall_stature_for_patrick.tsv", sep="\t", quote=F, row.names=F)



